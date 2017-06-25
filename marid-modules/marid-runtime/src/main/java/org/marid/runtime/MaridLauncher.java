/*
 *
 */

package org.marid.runtime;

/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import org.jboss.logmanager.LogManager;
import org.marid.misc.Casts;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.TimeZone;
import java.util.concurrent.locks.LockSupport;

import static java.util.ServiceLoader.load;
import static java.util.logging.Level.*;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) throws Exception {
        // Use JBoss LogManager instead of JUL
        System.setProperty("java.util.logging.manager", LogManager.class.getName());

        // Use UTC
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // Context
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        final ConfigurableEnvironment env = context.getEnvironment();
        if (args.length > 0) {
            env.getPropertySources().addAfter("commandLine", new SimpleCommandLinePropertySource(args));
        }
        {
            final Resource applicationProperties = context.getResource("application.properties");
            if (applicationProperties.exists()) {
                env.getPropertySources().addAfter("application", new ResourcePropertySource(applicationProperties));
            }
        }
        try {
            for (final ApplicationContextInitializer<?> initializer : load(ApplicationContextInitializer.class)) {
                final ApplicationContextInitializer<ConfigurableApplicationContext> i = Casts.cast(initializer);
                try {
                    i.initialize(context);
                    log(INFO, "Processed {0}", initializer.getClass().getSimpleName());
                } catch (Exception x) {
                    log(SEVERE, "Unable to run {0}", x, initializer);
                    return;
                }
            }

            context.refresh();
            context.start();
        } catch (Exception e) {
            log(SEVERE, "Container initialization failed", e);
            return;
        }

        // Input buffer
        final StringBuilder buffer = new StringBuilder();
        final Reader reader = new InputStreamReader(System.in);

        // Command processing loop
        try {
            COMMANDS:
            while (context.isActive() && !Thread.interrupted()) {
                while (reader.ready()) {
                    final int c = reader.read();
                    if (c < 0) {
                        log(SEVERE, "Broken pipe");
                        break COMMANDS;
                    }
                    buffer.append((char) c);
                }
                final int i = buffer.indexOf(System.lineSeparator());
                if (i >= 0) {
                    final String line = buffer.substring(0, i).trim();
                    buffer.delete(0, i + System.lineSeparator().length());
                    switch (line) {
                        case "close":
                            break COMMANDS;
                        case "exit":
                            System.exit(1);
                            break COMMANDS;
                    }
                }
                LockSupport.parkNanos(100_000_000L);
            }
        } catch (Exception x) {
            log(WARNING, "Command processing error", x);
        } finally {
            context.close();
        }
    }
}
