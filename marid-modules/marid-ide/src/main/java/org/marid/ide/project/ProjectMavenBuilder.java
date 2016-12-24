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

import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import org.marid.Ide;
import org.marid.ide.common.IdeValues;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.maven.ProjectBuilder;
import org.marid.maven.ProjectBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder implements LogSupport {

    private final URLClassLoader classLoader;
    private final ObjectProvider<FxAction> projectBuildAction;
    private final HttpServer httpServer;
    private final ZipFile zip;

    @Autowired
    public ProjectMavenBuilder(IdeValues ideValues, ObjectProvider<FxAction> projectBuildAction) throws Exception {
        this.projectBuildAction = projectBuildAction;
        final String resource = String.format("marid-maven-%s.zip", ideValues.implementationVersion);
        final URL baseUrl = requireNonNull(Ide.classLoader.getResource(resource), "marid-maven is not found");
        zip = new ZipFile(new File(baseUrl.toURI()));
        httpServer = HttpServer.create(new InetSocketAddress(0), 5);
        final Set<String> jars = zip.stream()
                .filter(e -> e.getName().endsWith(".jar"))
                .peek(e -> httpServer.createContext("/" + e.getName(), ex -> {
                    ex.sendResponseHeaders(HTTP_OK, e.getSize());
                    try (final OutputStream os = ex.getResponseBody(); final InputStream is = zip.getInputStream(e)) {
                        final byte[] buffer = new byte[(int) e.getSize()];
                        for (int n = is.read(buffer); n >= 0; n = is.read(buffer)) {
                            os.write(buffer, 0, n);
                        }
                    } catch (Exception x) {
                        log(WARNING, "Transfer error", x);
                    }
                }))
                .map(ZipEntry::getName)
                .collect(Collectors.toSet());
        httpServer.start();
        final List<URL> urls = new ArrayList<>(jars.size());
        final InetAddress localhost = InetAddress.getByAddress(InetAddress.getLocalHost().getAddress());
        final InetSocketAddress address = new InetSocketAddress(localhost, httpServer.getAddress().getPort());
        for (final String jar : jars) {
            urls.add(new URL("http:/" + address + "/" + jar));
        }
        classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    @PreDestroy
    private void destroy() throws IOException {
        try (final ZipFile z = zip; final URLClassLoader u = classLoader) {
            log(INFO, "Closing {0} and {1}", z, u);
        } finally {
            httpServer.stop(1);
        }
    }

    public Thread build(ProjectProfile profile, Consumer<Map<String, Object>> resultConsumer, Consumer<LogRecord> logConsumer) {
        final Thread thread = new Thread(() -> {
            Platform.runLater(() -> projectBuildAction.getObject().setDisabled(true));
            for (final ProjectBuilderFactory factory : ServiceLoader.load(ProjectBuilderFactory.class)) {
                final ProjectBuilder projectBuilder = factory.newBuilder(profile.getPath(), logConsumer)
                        .goals("clean", "install")
                        .profiles("conf");
                projectBuilder.build(resultConsumer);
            }
            Platform.runLater(() -> projectBuildAction.getObject().setDisabled(false));
        });
        thread.setContextClassLoader(classLoader);
        thread.start();
        return thread;
    }
}
