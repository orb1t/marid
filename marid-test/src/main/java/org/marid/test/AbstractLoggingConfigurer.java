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

package org.marid.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractLoggingConfigurer {

    public AbstractLoggingConfigurer(Level level) {
        final LogManager logManager = LogManager.getLogManager();
        final StringBuilder builder = new StringBuilder();
        final Formatter formatter = new Formatter(builder);
        formatter.format(".level=%s\n", level);
        formatter.format("handlers=%s\n", ConsoleHandler.class.getCanonicalName());
        formatter.format("%s.level=%s\n", ConsoleHandler.class.getCanonicalName(), level);
        final InputStream is = new ByteArrayInputStream(builder.toString().getBytes(ISO_8859_1));
        try {
            logManager.readConfiguration(is);
        } catch (IOException x) {
            x.printStackTrace(System.err);
        }
    }
}
