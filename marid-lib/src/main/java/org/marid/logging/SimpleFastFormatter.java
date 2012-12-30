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
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import static java.util.Calendar.*;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import org.marid.io.AppendableWriter;

/**
 * Simple fast formatter.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class SimpleFastFormatter extends FastFormatter {

    private static final TimeZone timeZone;
    private static final boolean showMillis;
    private static final boolean showLogger;
    private static final boolean showThread;
    private static final boolean threadName;

    static {
        LogManager manager = LogManager.getLogManager();
        String pfx = SimpleFastFormatter.class.getName();
        String z = manager.getProperty(pfx + ".timezone");
        timeZone = z != null ? TimeZone.getTimeZone(z) : TimeZone.getDefault();
        showMillis = "true".equals(manager.getProperty(pfx + ".showMillis"));
        showLogger = "true".equals(manager.getProperty(pfx + ".showLogger"));
        showThread = "true".equals(manager.getProperty(pfx + ".showThread"));
        threadName = "true".equals(manager.getProperty(pfx + ".threadName"));
    }

    @Override
    public void format(LogRecord r, Appendable a) throws IOException {
        Calendar c = Calendar.getInstance(timeZone, Locale.getDefault());
        c.setTimeInMillis(r.getMillis());
        a.append(Integer.toString(c.get(YEAR)));
        a.append('-');
        int n = c.get(MONTH) + 1;
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append('-');
        n = c.get(DATE);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(' ');
        n = c.get(HOUR);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(':');
        n = c.get(MINUTE);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        a.append(':');
        n = c.get(SECOND);
        if (n < 10) {
            a.append('0');
        }
        a.append(Integer.toString(n));
        if (showMillis) {
            a.append('.');
            n = c.get(MILLISECOND);
            if (n < 10) {
                a.append("00");
            } else if (n < 100) {
                a.append('0');
            }
            a.append(Integer.toString(n));
        }
        if (showLogger) {
            a.append(' ');
            a.append(r.getLoggerName());
        }
        if (showThread) {
            a.append(' ');
            if (threadName) {
                ThreadMXBean tb = ManagementFactory.getThreadMXBean();
                a.append(tb.getThreadInfo(r.getThreadID()).getThreadName());
            } else {
                a.append(Integer.toString(r.getThreadID()));
            }
        }
        a.append(' ');
        a.append(r.getLevel().getName());
        a.append(' ');
        a.append(r.getMessage());
        a.append('\n');
        if (r.getThrown() != null) {
            try (PrintWriter pw = new PrintWriter(new AppendableWriter(a))) {
                r.getThrown().printStackTrace(pw);
            }
        }
    }
}
