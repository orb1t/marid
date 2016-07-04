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
package org.marid.l10n;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class L10n {

    public static final String MSGS = "res.messages";
    public static final String STRS = "res.strings";

    public static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(String b, Locale l, String f, ClassLoader ld, boolean r) throws IllegalAccessException, InstantiationException, IOException {
            return getResourceBundle(ld, toResourceName(toBundleName(b, l), "properties"), r);
        }

        @Override
        public List<String> getFormats(String baseName) {
            return FORMAT_PROPERTIES;
        }

        private ResourceBundle getResourceBundle(ClassLoader ld, String resourceName, boolean reload) throws IOException {
            final ChainedPropertyResourceBundle bundle = new ChainedPropertyResourceBundle();
            for (final Enumeration<URL> e = ld.getResources(resourceName); e.hasMoreElements(); ) {
                bundle.load(e.nextElement(), !reload);
            }
            return bundle;
        }
    };

    public static String s(String key, Object... ps) {
        return s(Locale.getDefault(), key, ps);
    }

    public static String s(Locale locale, String key, Object... ps) {
        return s(getBundle(STRS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL), key, ps);
    }

    public static void s(Locale locale, String key, Appendable out, Object... ps) {
        s(getBundle(STRS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL), out, key, ps);
    }

    public static void s(Locale locale, String key, Formatter formatter, Object... ps) {
        s(getBundle(STRS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL), formatter, key, ps);
    }

    public static String m(String k, Object... v) {
        return m(Locale.getDefault(), k, v);
    }

    public static String m(Locale locale, String k, Object... v) {
        return m(getBundle(MSGS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL), k, v);
    }

    public static void m(Locale locale, String k, StringBuffer buffer, Object... v) {
        m(getBundle(MSGS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL), buffer, k, v);
    }

    public static ResourceBundle getMessagesBundle(Locale locale) {
        return getBundle(MSGS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL);
    }

    public static ResourceBundle getStringsBundle(Locale locale) {
        return getBundle(STRS, locale, Thread.currentThread().getContextClassLoader(), UTF8_CONTROL);
    }

    private static void m(ResourceBundle b, StringBuffer buf, String key, Object... v) {
        if (key == null) {
            return;
        }
        final String r = b.containsKey(key) ? b.getString(key) : key;
        if (v == null || v.length == 0) {
            buf.append(r);
        } else {
            try {
                new MessageFormat(r, b.getLocale()).format(v, buf, null);
            } catch (Exception x) {
                buf.append('!').append(r);
            }
        }
    }

    private static String m(ResourceBundle b, String key, Object... v) {
        final StringBuffer buffer = new StringBuffer(key.length());
        m(b, buffer, key, v);
        return buffer.toString();
    }

    private static void s(ResourceBundle b, Formatter fmt, String key, Object... v) {
        if (key == null) {
            return;
        }
        final String r = b.containsKey(key) ? b.getString(key) : key;
        if (v == null || v.length == 0) {
            fmt.format("%s", r);
        } else {
            try {
                fmt.format(b.getLocale(), r, v);
            } catch (Exception x) {
                fmt.format("!%s", r);
            }
        }
    }

    private static void s(ResourceBundle b, Appendable buf, String key, Object... v) {
        final Formatter formatter = new Formatter(buf);
        s(b, formatter, key, v);
    }

    private static String s(ResourceBundle b, String key, Object... v) {
        final StringBuilder builder = new StringBuilder(key.length());
        s(b, builder, key, v);
        return builder.toString();
    }
}
