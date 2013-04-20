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

package org.marid.logging;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.util.Calendar.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextFormatter extends Formatter {

    private final Calendar calendar = new GregorianCalendar();
    private final StringBuffer buffer = new StringBuffer();

    @Override
    public String format(LogRecord record) {
        int len = buffer.length();
        buffer.setLength(0);
        if (len > 65536) {
            buffer.trimToSize();
        }
        calendar.setTimeInMillis(record.getMillis());
        buffer.append(calendar.get(YEAR));
        buffer.append('-');
        append(calendar.get(MONTH) + 1, 2);
        buffer.append('-');
        append(calendar.get(DATE), 2);
        buffer.append(' ');
        append(calendar.get(HOUR_OF_DAY), 2);
        buffer.append(':');
        append(calendar.get(MINUTE), 2);
        buffer.append(':');
        append(calendar.get(SECOND), 2);
        buffer.append('.');
        append(calendar.get(MILLISECOND), 3);
        buffer.append(' ');
        if (record.getParameters() == null || record.getParameters().length == 0) {
            buffer.append(record.getMessage());
        } else {
            MessageFormat mf = new MessageFormat(record.getMessage());
            mf.format(record.getParameters(), buffer, null);
        }
        return buffer.toString();
    }

    private void append(int value, int width) {
        String textValue = Integer.toString(value);
        int len = width - textValue.length();
        for (int i = 0; i < len; i++) {
            buffer.append('0');
        }
        buffer.append(textValue);
    }
}
