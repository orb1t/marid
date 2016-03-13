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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utils {

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

    public static ClassLoader currentClassLoader() {
        return Thread.currentThread().getContextClassLoader();
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

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object x) {
        return (T) x;
    }

    public static Class<?> wrapperType(Class<?> primitiveType) {
        switch (primitiveType.getName()) {
            case "int":
                return Integer.class;
            case "boolean":
                return Boolean.class;
            case "long":
                return Long.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "char":
                return Character.class;
            case "short":
                return Short.class;
            case "byte":
                return Byte.class;
            case "void":
                return Void.class;
            default:
                throw new IllegalArgumentException(primitiveType.getName());
        }
    }

    public static <T> T call(Callable<T> callable) throws IllegalStateException {
        try {
            return callable.call();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T call(String exceptionMessage, Callable<T> callable) throws IllegalStateException {
        try {
            return callable.call();
        } catch (Exception x) {
            throw new IllegalStateException(exceptionMessage, x);
        }
    }

    public static void callWithTime(TimeUnit timeUnit, Runnable task, LongConsumer timeConsumer) {
        final long startTime = System.nanoTime();
        task.run();
        final long time = System.nanoTime() - startTime;
        timeConsumer.accept(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }

    public static void callWithTime(Runnable task, LongConsumer timeConsumer) {
        callWithTime(TimeUnit.MILLISECONDS, task, timeConsumer);
    }

    public static <T> T callWithTime(TimeUnit timeUnit, Callable<T> task, BiConsumer<Long, Exception> timeConsumer) {
        final long startTime = System.nanoTime();
        Exception exception = null;
        T result = null;
        try {
            result = task.call();
        } catch (Exception x) {
            exception = x;
        }
        final long time = System.nanoTime() - startTime;
        timeConsumer.accept(timeUnit.convert(time, TimeUnit.NANOSECONDS), exception);
        return result;
    }

    public static <T> T callWithTime(Callable<T> task, BiConsumer<Long, Exception> timeConsumer) {
        return callWithTime(TimeUnit.MILLISECONDS, task, timeConsumer);
    }

    public static void merge(Properties properties, String... resources) throws IOException {
        for (final String resource : resources) {
            try (final InputStream inputStream = currentClassLoader().getResourceAsStream(resource)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        }
    }
}
