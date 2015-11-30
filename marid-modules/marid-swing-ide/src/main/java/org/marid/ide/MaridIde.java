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

package org.marid.ide;

import org.jboss.logmanager.LogManager;
import org.marid.lifecycle.ShutdownThread;
import org.marid.logging.Logging;
import org.marid.spring.CommandLinePropertySource;
import org.marid.swing.log.SwingHandler;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde {

    static {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        LogManager.getLogManager().reset();
    }

    public static void main(String[] args) throws Exception {
        start(EventQueue::invokeLater, args);
    }

    public static final Logger LOGGER = Logger.getLogger("marid");
    public static final ClassPathXmlApplicationContext CONTEXT = new ClassPathXmlApplicationContext();

    public static void start(Consumer<Runnable> starter, String... args) throws Exception {
        Logging.rootLogger().addHandler(new SwingHandler());
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(LOGGER, WARNING, "Uncaught exception in {0}", e, t));
        CONTEXT.addApplicationListener(event -> {
            if (event instanceof ContextStartedEvent) {
                new ShutdownThread(CONTEXT).start();
            }
            log(LOGGER, INFO, "{0}", null, event);
        });
        final CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(args);
        CONTEXT.getEnvironment().getPropertySources().addFirst(commandLinePropertySource);
        CONTEXT.setConfigLocation("classpath*:/META-INF/marid/*.xml");
        starter.accept(() -> {
            CONTEXT.refresh();
            CONTEXT.start();
        });
    }
}
