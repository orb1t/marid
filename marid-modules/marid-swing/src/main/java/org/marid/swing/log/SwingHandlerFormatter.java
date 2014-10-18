/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.log;

import java.sql.Time;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.util.function.Function.identity;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandlerFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        final StringBuffer buffer = new StringBuffer(128);
        buffer.append(new Time(record.getMillis()).toString());
        buffer.append(' ');
        m(Locale.getDefault(), record.getMessage(), buffer, identity(), record.getParameters());
        if (record.getThrown() != null) {
            buffer.append(' ');
            buffer.append(record.getThrown().getMessage());
        }
        return buffer.toString();
    }
}
