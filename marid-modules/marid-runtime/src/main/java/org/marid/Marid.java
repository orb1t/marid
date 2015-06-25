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

import org.marid.groovy.GroovyRuntime;
import org.marid.lifecycle.MaridRunner;
import org.marid.lifecycle.ShutdownThread;
import org.marid.spring.CommandLinePropertySource;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextStartedEvent;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.LogSupport.Log.log;
import static org.marid.util.Utils.currentClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class Marid {

    public static final Logger LOGGER = Logger.getLogger("marid");
    private static final ThreadLocal<AnnotationConfigApplicationContext> CONTEXT_ITL = new InheritableThreadLocal<>();

    static {
        setCurrentContext(new AnnotationConfigApplicationContext());
    }

    private static void applicationEventListener(ApplicationEvent event) {
        if (event instanceof ContextStartedEvent) {
            new ShutdownThread(getCurrentContext()).start();
        }
        log(LOGGER, INFO, "{0}", null, event);
    }

    private static void loadSysProperties() throws Exception {
        for (final Enumeration<URL> e = currentClassLoader().getResources("sys.properties"); e.hasMoreElements(); ) {
            try (final InputStreamReader reader = new InputStreamReader(e.nextElement().openStream(), UTF_8)) {
                final Properties properties = new Properties();
                properties.load(reader);
                for (final String name : properties.stringPropertyNames()) {
                    System.setProperty(name, properties.getProperty(name));
                }
            }
        }
    }

    public static void start(Consumer<Runnable> starter, String... args) throws Exception {
        getCurrentContext().setClassLoader(GroovyRuntime.CLASS_LOADER);
        Thread.currentThread().setContextClassLoader(GroovyRuntime.CLASS_LOADER);
        loadSysProperties();
        LogManager.getLogManager().reset();
        LogManager.getLogManager().readConfiguration();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(LOGGER, WARNING, "Uncaught exception in {0}", e, t));
        getCurrentContext().addApplicationListener(Marid::applicationEventListener);
        final CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(args);
        getCurrentContext().getEnvironment().getPropertySources().addFirst(commandLinePropertySource);
        MaridRunner.runMaridRunners(getCurrentContext(), args);
        starter.accept(() -> {
            getCurrentContext().refresh();
            getCurrentContext().start();
        });
    }

    public static void setCurrentContext(AnnotationConfigApplicationContext currentContext) {
        CONTEXT_ITL.set(currentContext);
    }

    public static AnnotationConfigApplicationContext getCurrentContext() {
        return CONTEXT_ITL.get();
    }

    public static void main(String... args) throws Exception {
        start(Runnable::run, args);
    }
}
