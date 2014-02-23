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

package org.marid.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utils {

    public static ClassLoader getClassLoader(Class<?> c) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? c.getClassLoader() : classLoader;
    }

    public static <T> T newInstance(Class<T> type, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return type.cast(getClassLoader(type).loadClass(className).newInstance());
    }

    public static URL getResource(String format, Object... args) {
        return getClassLoader(Utils.class).getResource(String.format(format, args));
    }

    public static Properties loadProperties(Class<?> c, String path) throws IOException {
        return loadProperties(getClassLoader(c).getResource(path));
    }

    public static Properties loadProperties(URL url) throws IOException {
        final Properties properties = new Properties();
        if (url != null) {
            try (final Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
        return properties;
    }
}
