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

import org.codehaus.plexus.util.FileUtils;
import org.marid.logging.LogSupport;
import org.marid.maven.ProjectBuilder;
import org.marid.maven.ProjectBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder implements LogSupport {

    private final Path tempDirectory;
    private final URLClassLoader classLoader;

    public ProjectMavenBuilder(@Value("${implementation.version}") String version) throws IOException, URISyntaxException {
        this.tempDirectory = Files.createTempDirectory("projectBuilder");
        final String resource = String.format("marid-maven-%s.zip", version);
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        final URL url = Objects.requireNonNull(contextLoader.getResource(resource));
        try (final FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(url.toURI()), contextLoader)) {
            final List<URL> urls = new ArrayList<>();
            for (final Path root : fileSystem.getRootDirectories()) {
                try (final Stream<Path> stream = Files.list(root)) {
                    final List<Path> paths = stream.collect(Collectors.toList());
                    for (final Path path : paths) {
                        final Path target = tempDirectory.resolve(path.getFileName().toString());
                        Files.copy(path, target);
                        log(INFO, "Copied {0} to {1}", path, target);
                        urls.add(target.toUri().toURL());
                    }
                }
            }
            classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
        }
    }

    @PreDestroy
    private void destroy() throws IOException {
        classLoader.close();
        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    public Thread build(ProjectProfile profile, Consumer<Map<String, Object>> resultConsumer, Consumer<LogRecord> logConsumer) {
        final Thread thread = new Thread(() -> {
            for (final ProjectBuilderFactory factory : ServiceLoader.load(ProjectBuilderFactory.class)) {
                final ProjectBuilder projectBuilder = factory.newBuilder(profile.getPath(), logConsumer)
                        .goals("clean", "install")
                        .profiles("conf");
                projectBuilder.build(resultConsumer);
            }
        });
        thread.setContextClassLoader(classLoader);
        thread.start();
        return thread;
    }
}
