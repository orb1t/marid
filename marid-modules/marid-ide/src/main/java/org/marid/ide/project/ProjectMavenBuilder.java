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

import javafx.application.Platform;
import org.marid.Ide;
import org.marid.ide.common.IdeCommons;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.maven.ProjectBuilder;
import org.marid.maven.ProjectBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder extends URLStreamHandler implements LogSupport {

    private final URLClassLoader classLoader;
    private final FxAction projectBuildAction;
    private final ZipFile zip;

    @Autowired
    public ProjectMavenBuilder(IdeCommons commons, FxAction projectBuildAction) throws Exception {
        this.projectBuildAction = projectBuildAction;
        final String resource = String.format("marid-maven-%s.zip", commons.ideValues.implementationVersion);
        final URL baseUrl = requireNonNull(Ide.classLoader.getResource(resource), "marid-maven is not found");
        zip = new ZipFile(new File(baseUrl.toURI()));
        final Set<String> jars = zip.stream()
                .filter(e -> e.getName().endsWith(".jar"))
                .map(ZipEntry::getName)
                .collect(Collectors.toSet());
        final List<URL> urls = new ArrayList<>(jars.size());
        commons.urlHandlerFactory.register("mvn", this);
        for (final String jar : jars) {
            urls.add(new URL("mvn:///" + jar));
        }
        classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    @PreDestroy
    private void destroy() throws IOException {
        try (final ZipFile z = zip; final URLClassLoader u = classLoader) {
            log(INFO, "Closing {0} and {1}", z, u);
        }
    }

    public Thread build(ProjectProfile profile, Consumer<Map<String, Object>> resultConsumer, Consumer<LogRecord> logConsumer) {
        final Thread thread = new Thread(() -> {
            Platform.runLater(() -> projectBuildAction.setDisabled(true));
            for (final ProjectBuilderFactory factory : ServiceLoader.load(ProjectBuilderFactory.class)) {
                final ProjectBuilder projectBuilder = factory.newBuilder(profile.getPath(), logConsumer)
                        .goals("clean", "install")
                        .profiles("conf");
                projectBuilder.build(resultConsumer);
            }
            Platform.runLater(() -> projectBuildAction.setDisabled(false));
        });
        thread.setContextClassLoader(classLoader);
        thread.start();
        return thread;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new URLConnection(url) {

            private ZipEntry entry = zip.getEntry(url.getFile().substring(1));

            @Override
            public void connect() throws IOException {
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return zip.getInputStream(entry);
            }

            @Override
            public int getContentLength() {
                return entry == null ? -1 : (int) entry.getSize();
            }
        };
    }
}
