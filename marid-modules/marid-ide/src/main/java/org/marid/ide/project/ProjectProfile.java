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

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jmlspecs.annotation.Immutable;
import org.marid.logging.LogSupport;
import org.marid.util.Builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.apache.commons.lang3.SystemUtils.USER_HOME;

/**
 * @author Dmitry Ovchinnikov
 */
@Immutable
public class ProjectProfile implements LogSupport {

    private final Model model;
    private final Path path;
    private final Path pomFile;
    private final Path src;
    private final Path target;
    private final Path srcMain;
    private final Path srcTest;
    private final Path srcMainJava;
    private final Path srcMainResources;
    private final Path srcTestJava;
    private final Path srcTestResources;

    public ProjectProfile(String name) {
        path = Paths.get(USER_HOME, "marid", "profiles", name);
        pomFile = path.resolve("pom.xml");
        src = path.resolve("src");
        target = path.resolve("target");
        srcMain = src.resolve("main");
        srcTest = src.resolve("test");
        srcMainJava = srcMain.resolve("java");
        srcMainResources = srcMain.resolve("resources");
        srcTestJava = srcTest.resolve("java");
        srcTestResources = srcTest.resolve("resources");
        model = loadModel();
    }

    private Model loadModel() {
        try (final InputStream is = Files.newInputStream(pomFile)) {
            final MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(is);
        } catch (NoSuchFileException x) {
            log(FINE, "There is no {0} file", x.getFile());
        } catch (IOException x) {
            log(WARNING, "Unable to read pom.xml", x);
        } catch (XmlPullParserException x) {
            log(WARNING, "Unable to parse pom.xml", x);
        }
        return new Model();
    }

    public Model getModel() {
        return model;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return path.getFileName().toString();
    }

    private void createFileStructure() {
        try {
            for (final Path dir : Arrays.asList(srcMainJava, srcMainResources, srcTestJava, srcTestResources)) {
                Files.createDirectories(dir);
            }
        } catch (Exception x) {
            log(WARNING, "Unable to create file structure", x);
        }
    }

    private void initModelPrerequisites() {
        model.setPrerequisites(new Builder<>(new Prerequisites()).$(Prerequisites::setMaven, "3.3").build());
        model.getProperties().setProperty("project.build.sourceEncoding", "UTF-8");
        model.getProperties().setProperty("project.reporting.outputEncoding", "UTF-8");
        if (model.getBuild() == null) {
            model.setBuild(new Build());
        }
        {
            final Plugin execMavenPlugin = model.getBuild().getPlugins().stream()
                    .filter(p -> "org.codehaus.mojo".equals(p.getGroupId()))
                    .filter(p -> "exec-maven-plugin".equals(p.getArtifactId()))
                    .findAny()
                    .orElseGet(() -> {
                        final Plugin plugin = new Plugin();
                        plugin.setGroupId("org.codehaus.mojo");
                        plugin.setArtifactId("exec-maven-plugin");
                        plugin.setVersion("1.4.0");
                        model.getBuild().getPlugins().add(plugin);
                        return plugin;
                    });
            final PluginExecution runInIdeExecution = execMavenPlugin.getExecutions().stream()
                    .filter(e -> "run-in-ide".equals(e.getId()))
                    .findAny()
                    .orElseGet(() -> {
                        final PluginExecution execution = new PluginExecution();
                        execution.setId("run-in-ide");
                        execMavenPlugin.getExecutions().add(execution);
                        return execution;
                    });
            runInIdeExecution.setPhase(null);
            runInIdeExecution.setGoals(new ArrayList<>(Collections.singletonList("exec")));
        }
    }

    private void savePomFile() {
        initModelPrerequisites();
        try (final OutputStream os = Files.newOutputStream(pomFile)) {
            final MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(os, model);
        } catch (IOException x) {
            log(WARNING, "Unable to save {0}", x, pomFile);
        }
    }

    public void save() {
        createFileStructure();
        savePomFile();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProjectProfile && (((ProjectProfile) obj).getName().equals(this.getName()));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
