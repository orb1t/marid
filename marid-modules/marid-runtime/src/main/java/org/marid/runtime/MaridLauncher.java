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
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ServiceLoader;
import java.util.TimeZone;
import java.util.concurrent.locks.LockSupport;

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

        // Weld container
        final WeldContainer container;
        try {
            final Weld weld = new Weld().disableDiscovery();

            for (final WeldInitializer weldInitializer : ServiceLoader.load(WeldInitializer.class)) {
                try {
                    weldInitializer.initialize(weld);
                    log(INFO, "Processed {0}", weldInitializer.getClass().getSimpleName());
                } catch (Exception x) {
                    log(SEVERE, "Unable to run {0}", x, weldInitializer);
                    return;
                }
            }

            container = weld.initialize();
        } catch (Exception e) {
            log(SEVERE, "Container initialization failed", e);
            return;
        }

        // Initialize startup beans
        log(INFO, "Initialized {0} classes", container.select(new StartupLiteral()).stream().count());

        // Input buffer
        final StringBuilder buffer = new StringBuilder();
        final Reader reader = new InputStreamReader(System.in);

        // Command processing loop
        try {
            COMMANDS:
            while (container.isRunning() && !Thread.interrupted()) {
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
            container.close();
        }
    }
}
