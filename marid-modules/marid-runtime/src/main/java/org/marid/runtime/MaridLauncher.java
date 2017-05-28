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
import java.util.concurrent.locks.LockSupport;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) throws Exception {
        // Use JBoss LogManager instead of JUL
        System.setProperty("java.util.logging.manager", LogManager.class.getName());

        // Weld container
        final WeldContainer container;
        try {
            final Weld weld = new Weld(args.length > 0 ? args[0] : "marid");
            container = weld.initialize();
            log(INFO, "Initialized {0}", weld.getContainerId());
        } catch (Exception e) {
            log(SEVERE, "Container initialization failed", e);
            return;
        }

        // Input buffer
        final StringBuilder buffer = new StringBuilder();
        final Reader reader = new InputStreamReader(System.in);

        // Command processing loop
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
                        container.close();
                        break;
                    case "exit":
                        container.close();
                        break COMMANDS;
                }
            }
            LockSupport.parkNanos(100_000_000L);
        }
        log(INFO, "Exited {0}", container.getId());
    }
}
