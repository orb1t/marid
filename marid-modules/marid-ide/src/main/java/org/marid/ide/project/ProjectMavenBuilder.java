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
import org.codehaus.plexus.util.FileUtils;
import org.marid.Ide;
import org.marid.ide.common.IdeValues;
import org.marid.jfx.action.FxAction;
import org.marid.maven.ProjectBuilder;
import org.marid.maven.ProjectBuilderFactory;
import org.marid.status.MaridStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder {

    private final Path tempDirectory;
    private final URLClassLoader classLoader;
    private final ObjectProvider<FxAction> projectBuildAction;

    @Autowired
    public ProjectMavenBuilder(IdeValues ideValues,
                               MaridStatus maridStatus,
                               ObjectProvider<FxAction> projectBuildAction) throws IOException, URISyntaxException {
        this.projectBuildAction = projectBuildAction;
        this.tempDirectory = Files.createTempDirectory("projectBuilder");
        final String resource = String.format("marid-maven-%s.zip", ideValues.implementationVersion);
        final URL url = Objects.requireNonNull(Ide.classLoader.getResource(resource), "marid-maven artifact is not found");
        try (final ZipInputStream zipInputStream = new ZipInputStream(url.openStream(), StandardCharsets.UTF_8)) {
            final List<URL> urls = new ArrayList<>();
            for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
                if (entry.getName().endsWith(".jar")) {
                    final Path target = tempDirectory.resolve(entry.getName());
                    try (final OutputStream os = Files.newOutputStream(target)) {
                        final byte[] buffer = new byte[65536];
                        while (true) {
                            final int n = zipInputStream.read(buffer);
                            if (n < 0) {
                                break;
                            }
                            os.write(buffer, 0, n);
                        }
                    }
                    final String name = entry.getName();
                    maridStatus.doWithSession(session -> session.showMessage("Copied {0} to {1}", name, target));
                    urls.add(target.toUri().toURL());
                }
                zipInputStream.closeEntry();
            }
            classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
        }
        maridStatus.doWithSession(session -> session.showMessage("marid-maven copied to temporary directory"));
    }

    @PreDestroy
    private void destroy() throws IOException {
        classLoader.close();
        FileUtils.deleteDirectory(tempDirectory.toFile());
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
