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

import com.google.common.io.ByteStreams;
import javafx.application.Platform;
import org.marid.Ide;
import org.marid.ide.common.IdeUrlHandlerFactory;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.maven.ProjectBuilder;
import org.marid.maven.ProjectBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.marid.misc.Calls.func;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder extends URLStreamHandler implements LogSupport {

    private final URLClassLoader classLoader;
    private final FxAction projectBuildAction;
    private final Map<String, byte[]> zipData = new HashMap<>();

    @Autowired
    public ProjectMavenBuilder(IdeUrlHandlerFactory urlHandlerFactory, FxAction projectBuildAction) throws Exception {
        this.projectBuildAction = projectBuildAction;
        try (final InputStream is = Ide.classLoader.getResourceAsStream("marid-maven.zip")) {
            if (is == null) {
                throw new IllegalStateException("marid-maven.zip is not found");
            }
            try (final ZipInputStream stream = new ZipInputStream(is, UTF_8)) {
                for (ZipEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
                    zipData.put(entry.getName(), ByteStreams.toByteArray(stream));
                }
            }
        }
        urlHandlerFactory.register("mvn", this);
        final URL[] urls = zipData.keySet().stream().map(func(e -> () -> new URL("mvn:///" + e))).toArray(URL[]::new);
        classLoader = new URLClassLoader(urls);
    }

    Thread build(ProjectProfile profile, Consumer<Map<String, Object>> consumer, Consumer<LogRecord> logConsumer) {
        final Thread thread = new Thread(() -> {
            Platform.runLater(() -> projectBuildAction.setDisabled(true));
            for (final ProjectBuilderFactory factory : ServiceLoader.load(ProjectBuilderFactory.class)) {
                final ProjectBuilder projectBuilder = factory.newBuilder(profile.getPath(), logConsumer)
                        .goals("clean", "install")
                        .profiles("conf");
                projectBuilder.build(consumer);
            }
            Platform.runLater(() -> projectBuildAction.setDisabled(false));
        });
        thread.setContextClassLoader(classLoader);
        thread.start();
        return thread;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        final byte[] data = zipData.get(url.getFile().substring(1));
        return new URLConnection(url) {
            @Override
            public void connect() throws IOException {
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(data);
            }

            @Override
            public int getContentLength() {
                return data.length;
            }
        };
    }
}
