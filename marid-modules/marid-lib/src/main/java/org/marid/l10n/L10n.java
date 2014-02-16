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

import org.marid.util.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.ResourceBundle.getBundle;

/**
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class L10n {

    public static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(String b, Locale l, String f, ClassLoader ld, boolean r) throws IllegalAccessException, InstantiationException, IOException {
            if (FORMAT_PROPERTIES.contains(f)) {
                final String bundleName = toBundleName(b, l);
                final String resourceName = toResourceName(bundleName, "properties");
                return getResourceBundle(ld, resourceName, r);
            } else {
                return super.newBundle(b, l, f, ld, r);
            }
        }

        private ResourceBundle getResourceBundle(ClassLoader ld, String resourceName, boolean reload) throws IOException {
            ResourceBundle resourceBundle = null;
            for (final Enumeration<URL> e = ld.getResources(resourceName); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                final URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(!reload);
                try (final Reader rd = new InputStreamReader(urlConnection.getInputStream(), UTF_8)) {
                    resourceBundle = new ChainedPropertyResourceBundle(resourceBundle, rd);
                }
            }
            return resourceBundle;
        }
    };

    public static final ResourceBundle SB, MB;

    static {
        final ClassLoader cl = Utils.getClassLoader(L10n.class);
        ResourceBundle sb, mb;
        try {
            sb = getBundle("res.strings", Locale.getDefault(), cl, UTF8_CONTROL);
        } catch (Exception x) {
            x.printStackTrace(System.err);
            sb = ResourceBundle.getBundle(L10n.class.getPackage().getName() + ".strings");
        }
        try {
            mb = getBundle("res.messages", Locale.getDefault(), cl, UTF8_CONTROL);
        } catch (Exception x) {
            x.printStackTrace(System.err);
            mb = ResourceBundle.getBundle(L10n.class.getPackage().getName() + ".messages");
        }
        SB = sb;
        MB = mb;
    }

    public static String s(String key, Object... ps) {
        final String r = SB.containsKey(key) ? SB.getString(key) : key;
        return ps == null || ps.length == 0 ? r : String.format(r, ps);
    }

    public static void s(String key, Appendable out, Object... ps) {
        final String r = SB.containsKey(key) ? SB.getString(key) : key;
        final Formatter formatter = new Formatter(out);
        formatter.format(r, ps);
    }

    public static void s(String key, Formatter formatter, Object... ps) {
        final String r = SB.containsKey(key) ? SB.getString(key) : key;
        formatter.format(r, ps);
    }

    public static String m(String k, Object... v) {
        final String r = MB.containsKey(k) ? MB.getString(k) : k;
        return v == null || v.length == 0 ? r : MessageFormat.format(r, v);
    }

    public static void m(String k, StringBuffer buffer, Object... v) {
        final String r = MB.containsKey(k) ? MB.getString(k) : k;
        if (v == null || v.length == 0) {
            buffer.append(r);
        } else {
            new MessageFormat(r).format(v, buffer, null);
        }
    }
}
