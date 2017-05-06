/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.logging;

import org.marid.io.AppendableWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.marid.IdePrefs.PREFERENCES;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogConfig extends Handler {

    public static final Logger ROOT_LOGGER;

    static {
        ROOT_LOGGER = Logger.getLogger("");
        ROOT_LOGGER.setLevel(Level.parse(PREFERENCES.get("logLevel", Level.INFO.getName())));
        ROOT_LOGGER.addHandler(new IdeLogConfig());
    }

    @Override
    public void publish(LogRecord record) {
        final StringBuffer builder = new StringBuffer();
        try (final Formatter formatter = new Formatter(builder)) {
            formatter.format("%tF %tT ", record.getMillis(), record.getMillis());
            formatter.flush();
            builder.append(level(record.getLevel()));
            builder.append(' ');
            builder.append(abbreviate(record.getLoggerName(), 36));
            builder.append(' ');
            m(Locale.getDefault(), record.getMessage(), builder, record.getParameters());
            builder.append(System.lineSeparator());
            if (record.getThrown() != null) {
                try (final PrintWriter printWriter = new PrintWriter(new AppendableWriter(builder))) {
                    record.getThrown().printStackTrace(printWriter);
                }
            }
        }
        System.out.append(builder);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    private static char level(Level level) {
        switch (level.intValue()) {
            case 800: return 'I';
            case 900: return 'W';
            case 1000: return 'E';
            case 300: return 'T';
            case 400: return 'F';
            case 500: return 'D';
            case 700: return 'C';
            default: return '*';
        }
    }

    private static char[] abbreviate(String logger, int width) {
        final char[] result = new char[width];
        if (logger != null) {
            final int pos = logger.lastIndexOf('.');
            final int offset = pos >= 0 ? pos + 1 : 0;
            final int len = Math.min(logger.length() - offset, width);
            for (int i = 0; i < len; i++) {
                result[i] = logger.charAt(i + offset);
            }
            Arrays.fill(result, len, width, ' ');
        } else {
            Arrays.fill(result, ' ');
        }
        return result;
    }
}
