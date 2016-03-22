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
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.util.Scanner;
import java.util.TimeZone;

import static java.lang.Thread.currentThread;
import static org.marid.runtime.MaridContextInitializer.applicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        final GenericApplicationContext context = applicationContext(currentThread().getContextClassLoader());
        context.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        final Thread shutdownHook = new Thread(context::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
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
