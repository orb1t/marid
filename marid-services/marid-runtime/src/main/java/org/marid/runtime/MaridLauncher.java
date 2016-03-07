/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.runtime;

import org.jboss.logmanager.LogManager;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.util.Scanner;
import java.util.TimeZone;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.addBeanFactoryPostProcessor(new MaridBeanFactoryPostProcessor());
        context.setConfigLocation("classpath*:/META-INF/marid/**/*.xml");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setAllowCircularReferences(false);
        context.setValidating(false);
        context.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        Runtime.getRuntime().addShutdownHook(new Thread(context::close));
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                try {
                    System.in.close();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        });
        context.refresh();
        context.start();
        try (final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();
                switch (line) {
                    case "dump":
                        Thread.getAllStackTraces().forEach((t, stes) -> {
                            System.err.println(t);
                            if (stes != null) {
                                for (final StackTraceElement e : stes) {
                                    System.err.format("%s %s.%s:%d%n",
                                            e.getFileName(),
                                            e.getClassName(),
                                            e.getMethodName(),
                                            e.getLineNumber());
                                }
                            }
                            System.err.println();
                        });
                        break;
                    case "exit":
                    case "quit":
                        context.close();
                        break;
                }
            }
        }
    }
}
