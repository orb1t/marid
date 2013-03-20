/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.logging;

import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Marid log formatter.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class FastFormatter extends Formatter {

    /**
     * Formats the record into print writer.
     *
     * @param r A log record.
     * @param a An appendable object.
     */
    public abstract void format(LogRecord r, Appendable a) throws IOException;

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        try {
            format(record, sb);
        } catch (IOException x) {
           return formatMessage(record);
        }
        return sb.toString();
    }
}
