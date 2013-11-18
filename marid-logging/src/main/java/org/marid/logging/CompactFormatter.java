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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static java.util.Calendar.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class CompactFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        final StringWriter sw = new StringWriter();
        final StringBuffer buffer = sw.getBuffer();
        final Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(record.getMillis());
        {
            final int hour = cal.get(HOUR_OF_DAY);
            if (hour < 10) buffer.append('0');
            buffer.append(hour).append(':');
        }
        {
            final int minute = cal.get(MINUTE);
            if (minute < 10) buffer.append('0');
            buffer.append(minute).append(':');
        }
        {
            final int second = cal.get(SECOND);
            if (second < 10) buffer.append('0');
            buffer.append(second).append('.');
        }
        {
            final int ms = cal.get(MILLISECOND);
            if (ms < 10) buffer.append("00");
            else if (ms < 100) buffer.append('0');
            buffer.append(ms).append(' ');
        }
        buffer.append(record.getLevel().getName())
                .append(' ')
                .append(record.getLoggerName())
                .append(' ');
        if (record.getParameters() != null) {
            try {
                final MessageFormat mf = new MessageFormat(record.getMessage());
                mf.format(record.getParameters(), buffer, null);
            } catch (Exception x) {
                x.printStackTrace(System.err);
            }
        } else {
            buffer.append(record.getMessage());
        }
        buffer.append(System.lineSeparator());
        if (record.getThrown() != null) {
            final PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
        }
        return buffer.toString();
    }
}
