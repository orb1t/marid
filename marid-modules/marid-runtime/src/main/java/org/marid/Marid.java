/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid;

import groovy.lang.GroovyCodeSource;
import org.marid.groovy.GroovyRuntime;
import org.marid.methods.LogMethods;
import org.marid.spring.CommandLinePropertySource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.marid.groovy.GroovyRuntime.SHELL;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Marid {

    public static final Logger LOGGER = Logger.getLogger("marid");
    public static final AnnotationConfigApplicationContext CONTEXT = new AnnotationConfigApplicationContext();

    static {
        Thread.currentThread().setContextClassLoader(GroovyRuntime.CLASS_LOADER);
        CONTEXT.setClassLoader(Thread.currentThread().getContextClassLoader());
    }

    public static void main(String... args) throws Exception {
        for (final Enumeration<URL> e = CONTEXT.getClassLoader().getResources("sys.properties"); e.hasMoreElements(); ) {
            try (final InputStreamReader reader = new InputStreamReader(e.nextElement().openStream(), UTF_8)) {
                final Properties properties = new Properties();
                properties.load(reader);
                for (final String name : properties.stringPropertyNames()) {
                    System.setProperty(name, properties.getProperty(name));
                }
            }
        }
        LogManager.getLogManager().reset();
        LogManager.getLogManager().readConfiguration();
        LogMethods.info(LOGGER, "Logging initialized");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> warning(LOGGER, "Uncaught exception in {0}", e, t));
        CONTEXT.addApplicationListener(e -> LogMethods.info(LOGGER, "{0}", e));
        final CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(args);
        CONTEXT.getEnvironment().getPropertySources().addFirst(commandLinePropertySource);
        for (final String file : commandLinePropertySource.getNonOptionArgs()) {
            for (final Enumeration<URL> e = CONTEXT.getClassLoader().getResources(file); e.hasMoreElements(); ) {
                SHELL.evaluate(new GroovyCodeSource(e.nextElement()));
            }
        }
        CONTEXT.refresh();
        CONTEXT.start();
    }
}
