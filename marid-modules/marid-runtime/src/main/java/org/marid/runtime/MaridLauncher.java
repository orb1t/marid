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

package org.marid.runtime;


import org.marid.io.Xmls;
import org.marid.runtime.context.MaridContext;
import org.marid.runtime.context.MaridRuntime;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) throws Exception {
        // Use UTC
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        // Context
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL beansXmlUrl = classLoader.getResource("META-INF/marid/beans.xml");
        if (beansXmlUrl == null) {
            throw new IllegalStateException("No beans.xml file found");
        }
        final MaridRuntime runtime;
        try (final Reader reader = new InputStreamReader(beansXmlUrl.openStream(), UTF_8)) {
            final AtomicReference<MaridContext> contextRef = new AtomicReference<>();
            Xmls.read(d -> contextRef.set(new MaridContext(d.getDocumentElement())), reader);
            runtime = new MaridRuntime(contextRef.get(), classLoader);
        }

        // Input buffer
        final StringBuilder buffer = new StringBuilder();
        final Reader reader = new InputStreamReader(System.in);

        // Command processing loop
        try {
            COMMANDS:
            while (runtime.isActive() && !Thread.interrupted()) {
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
            runtime.close();
        }
    }
}
