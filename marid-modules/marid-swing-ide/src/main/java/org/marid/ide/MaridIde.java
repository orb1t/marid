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

import org.marid.ide.context.BaseContext;
import org.marid.ide.context.GuiContext;
import org.marid.ide.context.ProfileContext;
import org.marid.lifecycle.MaridRunner;
import org.marid.lifecycle.ShutdownThread;
import org.marid.logging.Logging;
import org.marid.spring.CommandLinePropertySource;
import org.marid.swing.log.SwingHandler;
import org.marid.xml.XmlPersister;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextStartedEvent;

import java.awt.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde implements MaridRunner {

    public static void main(String[] args) throws Exception {
        start(EventQueue::invokeLater, args);
    }

    public static final Logger LOGGER = Logger.getLogger("marid");
    public static final AnnotationConfigApplicationContext CONTEXT = new AnnotationConfigApplicationContext();

    public static void start(Consumer<Runnable> starter, String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(LOGGER, WARNING, "Uncaught exception in {0}", e, t));
        CONTEXT.addApplicationListener(event -> {
            if (event instanceof ContextStartedEvent) {
                new ShutdownThread(CONTEXT).start();
            }
            log(LOGGER, INFO, "{0}", null, event);
        });
        final CommandLinePropertySource commandLinePropertySource = new CommandLinePropertySource(args);
        CONTEXT.getEnvironment().getPropertySources().addFirst(commandLinePropertySource);
        MaridRunner.runMaridRunners(CONTEXT, args);
        starter.accept(() -> {
            CONTEXT.refresh();
            CONTEXT.start();
        });
    }

    @Override
    public void run(AnnotationConfigApplicationContext context, String... args) throws Exception {
        Logging.rootLogger().addHandler(new SwingHandler());
        context.register(XmlPersister.class, BaseContext.class, ProfileContext.class, GuiContext.class);
    }
}
