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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utils {

    public static final ZoneId ZONE_ID = ZoneId.systemDefault();
    public static final SecureRandom RANDOM = new SecureRandom();

    public static <T> T newInstance(Class<T> type, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return type.cast(classLoader.loadClass(className).newInstance());
    }

    public static URL getResource(String format, Object... args) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResource(String.format(format, args));
    }

    public static Properties loadProperties(String path) throws IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return loadProperties(classLoader.getResource(path));
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

    public static URL getUrl(String file) throws MalformedURLException {
        try {
            return new URL(file);
        } catch (MalformedURLException x) {
            return Paths.get(file).toUri().toURL();
        }
    }

    public static URI getUri(String file) {
        try {
            return new URI(file);
        } catch (URISyntaxException x) {
            return Paths.get(file).toUri();
        }
    }

    public static BigInteger getUid(int len) {
        final byte[] num = new byte[len];
        RANDOM.nextBytes(num);
        return new BigInteger(1, num);
    }

    public static BigInteger getUid() {
        return getUid(16);
    }

    public static Class<?> wrapperType(Class<?> primitiveType) {
        switch (primitiveType.getName()) {
            case "int":     return Integer.class;
            case "boolean": return Boolean.class;
            case "long":    return Long.class;
            case "double":  return Double.class;
            case "float":   return Float.class;
            case "char":    return Character.class;
            case "short":   return Short.class;
            case "byte":    return Byte.class;
            case "void":    return Void.class;
            default:        throw new IllegalArgumentException(primitiveType.getName());
        }
    }
}
