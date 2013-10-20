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

package org.marid.test;

import org.marid.nio.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestUtils {

    public static URLClassLoader prepareClassLoader(Map<Class<?>, List<Class<?>>> map) throws IOException {
        final Path tempDir = Files.createTempDirectory("marid");
        try {
            final Path servicesDir = tempDir.resolve("META-INF").resolve("services");
            Files.createDirectories(servicesDir);
            for (final Entry<Class<?>, List<Class<?>>> e : map.entrySet()) {
                final Path path = servicesDir.resolve(e.getKey().getCanonicalName());
                try (final PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path, UTF_8))) {
                    for (final Class<?> c : e.getValue()) {
                        pw.println(c.getCanonicalName());
                    }
                }
            }
        } catch (IOException x) {
            FileUtils.remove(tempDir);
            throw x;
        }
        return new URLClassLoader(new URL[] {tempDir.toUri().toURL()});
    }

    public static URLClassLoader prepareClassLoader(Class<?> type, Class<?>... classes) throws IOException {
        return prepareClassLoader(Collections.<Class<?>, List<Class<?>>>singletonMap(
                type, Arrays.asList(classes)));
    }

    public static void clean(URLClassLoader classLoader) throws IOException {
        classLoader.close();
        for (final URL url : classLoader.getURLs()) {
            try {
                FileUtils.remove(Paths.get(url.toURI()));
            } catch (URISyntaxException x) {
                throw new FileNotFoundException(url.toString());
            }
        }
    }

    public static <T> T callWithClassLoader(Map<Class<?>, List<Class<?>>> map, Callable<T> callback) throws Exception {
        final URLClassLoader classLoader = prepareClassLoader(map);
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return callback.call();
        } finally {
            clean(classLoader);
        }
    }

    public static <T> T callWithClassLoader(Callable<T> callback, Class<?> type, Class<?>... classes) throws Exception {
        return callWithClassLoader(Collections.<Class<?>, List<Class<?>>>singletonMap(
                type, Arrays.asList(classes)), callback);
    }
}
