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

package org.marid.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClasspathUtils {

    public static Set<URL> getUrls(ClassLoader classLoader) {
        final Set<URL> urlSet = new LinkedHashSet<>();
        for (ClassLoader l = classLoader; l != null; l = l.getParent()) {
            if (l instanceof URLClassLoader) {
                final URL[] urls = ((URLClassLoader) l).getURLs();
                if (urls != null) {
                    urlSet.addAll(Arrays.asList(urls));
                }
            }
        }
        return urlSet;
    }

    public static void visitUrls(ClassLoader classLoader, Consumer<URL> consumer) {
        for (ClassLoader l = classLoader; l != null; l = l.getParent()) {
            if (l instanceof URLClassLoader) {
                final URL[] urls = ((URLClassLoader) l).getURLs();
                if (urls != null) {
                    for (final URL url : urls) {
                        consumer.accept(url);
                    }
                }
            }
        }
    }

    public static Properties loadProperties(String url) {
        final Properties properties = new Properties();
        try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(url)) {
            properties.load(inputStream);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
        return properties;
    }
}
