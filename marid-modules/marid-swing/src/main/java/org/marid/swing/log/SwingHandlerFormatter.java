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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandlerFormatter extends Formatter {

    private static final int CACHE_SIZE = 1024;
    private static final Map<Object, Object> CACHE = new IdentityHashMap<>();

    private static void cleanCache() {
        for (final Iterator<Map.Entry<Object, Object>> i = CACHE.entrySet().iterator(); i.hasNext() && CACHE.size() > CACHE_SIZE; ) {
            i.next();
            i.remove();
        }
    }

    private static Object get(Object key) {
        if (key == null
                || key.getClass().getPackage() == Byte.class.getPackage()
                || key.getClass().getPackage() == BigDecimal.class.getPackage()) {
            return key;
        }
        synchronized (CACHE) {
            final Object value = CACHE.computeIfAbsent(key, Object::toString);
            if (CACHE.size() > CACHE_SIZE) {
                cleanCache();
            }
            return value;
        }
    }

    private static Object[] transform(Object[] parameters) {
        final Object[] result = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            result[i] = get(parameters[i]);
        }
        return result;
    }

    @Override
    public String format(LogRecord record) {
        final StringWriter writer = new StringWriter(128);
        writer.append(new Time(record.getMillis()).toString());
        writer.append(' ');
        if (record.getParameters() == null || record.getParameters().length == 0) {
            writer.append(record.getMessage());
        } else {
            try {
                final Locale locale = record.getResourceBundle() != null
                        ? record.getResourceBundle().getLocale()
                        : Locale.getDefault();
                final MessageFormat messageFormat = new MessageFormat(record.getMessage(), locale);
                messageFormat.format(transform(record.getParameters()), writer.getBuffer(), null);
            } catch (Exception x) {
                writer.append(record.getMessage());
                writer.append(" : ");
                writer.append(Arrays.deepToString(transform(record.getParameters())));
            }
        }
        if (record.getThrown() != null) {
            writer.append(' ');
            writer.append(record.getThrown().getMessage());
        }
        return writer.toString();
    }
}
