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

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.util.Calendar.*;
import static org.marid.l10n.L10n.*;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandlerFormatter extends Formatter {

    private final Calendar calendar = new GregorianCalendar(Locale.ROOT);

    @Override
    public String format(LogRecord record) {
        calendar.setTimeInMillis(record.getMillis());
        StringBuffer buffer = new StringBuffer(Integer.toString(calendar.get(YEAR)));
        buffer.append('-');
        int field = calendar.get(MONTH) + 1;
        if (field < 10) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append('-');
        field = calendar.get(DATE);
        if (field < 10) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append(' ');
        field = calendar.get(HOUR_OF_DAY);
        if (field < 10) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append(':');
        field = calendar.get(MINUTE);
        if (field < 10) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append(':');
        field = calendar.get(SECOND);
        if (field < 10) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append('.');
        field = calendar.get(MILLISECOND);
        if (field < 10) {
            buffer.append("00");
        } else if (field < 100) {
            buffer.append('0');
        }
        buffer.append(field);
        buffer.append(' ');
        try {
            m(record.getMessage(), buffer, record.getParameters());
        } catch (Exception x) {
            x.printStackTrace();
            buffer.append(record.getMessage());
            buffer.append(": ");
            buffer.append(Arrays.toString(record.getParameters()));
        }
        return buffer.toString();
    }
}
