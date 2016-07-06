/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.project;

import org.marid.logging.LogSupport;
import org.marid.misc.Calls;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectCacheEntry implements AutoCloseable, LogSupport {

    private final ProjectProfile profile;
    private final Map<String, Class<?>> classMap = new HashMap<>();
    private URLClassLoader classLoader;

    public ProjectCacheEntry(ProjectProfile profile) {
        this.profile = profile;
        classLoader = classLoader();
    }

    @Nonnull
    @Override
    public Logger logger() {
        return profile.logger();
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void update() throws Exception {
        classMap.clear();
        try (final URLClassLoader classLoader = this.classLoader) {
            if (classLoader != null) {
                log(INFO, "Closing a class loader");
            }
        }
        classLoader = classLoader();
    }

    public Optional<Class<?>> getClass(String type) {
        return Optional.ofNullable(classMap.computeIfAbsent(type, t -> {
            try {
                return Class.forName(type, false, classLoader);
            } catch (Exception x) {
                return null;
            }
        }));
    }

    private void addJars(List<URL> urls, Path dir) {
        if (isDirectory(dir)) {
            final File[] files = dir.toFile().listFiles((d, name) -> name.endsWith(".jar"));
            urls.addAll(Stream.of(files).map(f -> Calls.call(() -> f.toURI().toURL())).collect(Collectors.toList()));
        }
    }

    private URLClassLoader classLoader() {
        final List<URL> urls = new ArrayList<>();
        addJars(urls, profile.getTarget().resolve("lib"));
        if (isDirectory(profile.getTarget().resolve("classes"))) {
            urls.add(Calls.call(() -> profile.getTarget().resolve("classes").toUri().toURL()));
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    @Override
    public void close() throws Exception {
        classMap.clear();
    }
}
