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

import com.google.common.io.MoreFiles;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;
import static org.marid.logging.Log.log;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectProfile {

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
    private final Path beansDirectory;
    private final Path repository;
    private final Logger logger;

    ProjectProfile(String name) {
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
        beansDirectory = srcMainResources.resolve("META-INF").resolve("marid");
        repository = path.resolve(".repo");
        logger = Logger.getLogger(getName());
        model = loadModel();
        model.setModelVersion("4.0.0");
        createFileStructure();
        init();
    }

    private void init() {
        if (model.getProfiles().stream().noneMatch(p -> "conf".equals(p.getId()))) {
            final Profile profile = new Profile();
            profile.setId("conf");
            model.getProfiles().add(profile);
        }
    }

    private Model loadModel() {
        try (final InputStream is = Files.newInputStream(pomFile)) {
            final MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(is);
        } catch (NoSuchFileException x) {
            // nop
        } catch (IOException x) {
            log(logger, WARNING, "Unable to read pom.xml", x);
        } catch (XmlPullParserException x) {
            log(logger, WARNING, "Unable to parse pom.xml", x);
        }
        return build(new Model(), model -> {
            model.setOrganization(new Organization());
            model.setName(getName());
            model.setArtifactId(getName());
            model.setGroupId("org.myproject");
            model.setVersion("1.0-SNAPSHOT");
        });
    }

    public Model getModel() {
        return model;
    }

    public Path getPath() {
        return path;
    }

    public Path getPomFile() {
        return pomFile;
    }

    public Path getRepository() {
        return repository;
    }

    public Path getBeansDirectory() {
        return beansDirectory;
    }

    public Path getSrc() {
        return src;
    }

    public Path getSrcMain() {
        return srcMain;
    }

    public Path getSrcMainJava() {
        return srcMainJava;
    }

    public Path getSrcMainResources() {
        return srcMainResources;
    }

    public Path getSrcTest() {
        return srcTest;
    }

    public Path getSrcTestJava() {
        return srcTestJava;
    }

    public Path getSrcTestResources() {
        return srcTestResources;
    }

    public Path getTarget() {
        return target;
    }

    public String getName() {
        return path.getFileName().toString();
    }

    public Path getJavaBaseDir(Path javaFile) {
        if (javaFile.startsWith(srcMainJava)) {
            return srcMainJava;
        } else if (javaFile.startsWith(srcTestJava)) {
            return srcTestJava;
        } else {
            return null;
        }
    }

    private void createFileStructure() {
        try {
            for (final Path dir : asList(srcMainJava, beansDirectory, srcTestJava, srcTestResources)) {
                Files.createDirectories(dir);
            }
            final Path loggingProperties = srcMainResources.resolve("logging.properties");
            if (Files.notExists(loggingProperties)) {
                final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
                try (final InputStream is = currentLoader.getResourceAsStream("logging/default.properties")) {
                    Files.copy(is, loggingProperties);
                }
            }
            final Path beansProperties = srcMainResources.resolve("beans.properties");
            if (Files.notExists(beansProperties)) {
                Files.createFile(beansProperties);
            }
        } catch (Exception x) {
            log(logger, WARNING, "Unable to create file structure", x);
        }
    }

    private void savePomFile() {
        try (final OutputStream os = Files.newOutputStream(pomFile)) {
            final MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(os, model);
        } catch (IOException x) {
            log(logger, WARNING, "Unable to save {0}", x, pomFile);
        }
    }

    public void save() {
        createFileStructure();
        savePomFile();
    }

    void delete() {
        try {
            MoreFiles.deleteRecursively(path);
        } catch (Exception x) {
            log(logger, WARNING, "Unable to delete {0}", x, getName());
        }
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
