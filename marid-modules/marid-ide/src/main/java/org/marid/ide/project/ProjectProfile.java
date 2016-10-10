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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.dependant.project.config.CommonTab;
import org.marid.logging.LogSupport;
import org.marid.spring.xml.BeanFile;
import org.marid.spring.xml.MaridBeanDefinitionLoader;
import org.marid.spring.xml.MaridBeanDefinitionSaver;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectProfile implements LogSupport {

    final Model model;
    final Path path;
    final Path pomFile;
    final Path src;
    final Path target;
    final Path srcMain;
    final Path srcTest;
    final Path srcMainJava;
    final Path srcMainResources;
    final Path srcTestJava;
    final Path srcTestResources;
    final Path beansDirectory;
    final Path repository;
    final Logger logger;
    final ObservableList<Pair<Path, BeanFile>> beanFiles;
    final ProjectCacheEntry cacheEntry;
    final BooleanProperty hmi;

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
        beanFiles = loadBeanFiles();
        init();
        cacheEntry = new ProjectCacheEntry(this);
        hmi = new SimpleBooleanProperty(isHmi());
        hmi.addListener((observable, oldValue, newValue) -> setHmi(newValue));
    }

    public URLClassLoader getClassLoader() {
        return cacheEntry.getClassLoader();
    }

    public boolean containsBean(String name) {
        return beanFiles.stream()
                .map(Pair::getValue)
                .anyMatch(f -> f.allBeans().anyMatch(b -> b.nameProperty().isEqualTo(name).get()));
    }

    public String generateBeanName(String name) {
        while (containsBean(name)) {
            name += "_new";
        }
        return name;
    }

    private void init() {
        if (model.getProfiles().stream().noneMatch(p -> "conf".equals(p.getId()))) {
            final Profile profile = new Profile();
            profile.setId("conf");
            model.getProfiles().add(profile);
        }
    }

    public BooleanProperty hmiProperty() {
        return hmi;
    }

    private boolean isHmi() {
        return model.getDependencies().stream().anyMatch(CommonTab::isHmi);
    }

    private boolean setHmi(boolean hmi) {
        if (hmi) {
            if (model.getDependencies().stream().anyMatch(CommonTab::isHmi)) {
                return false;
            } else {
                model.getDependencies().removeIf(CommonTab::isRuntime);
                final Dependency dependency = new Dependency();
                dependency.setGroupId("org.marid");
                dependency.setArtifactId("marid-hmi");
                dependency.setVersion("${marid.runtime.version}");
                model.getDependencies().add(dependency);
                return true;
            }
        } else {
            if (model.getDependencies().stream().anyMatch(CommonTab::isRuntime)) {
                return false;
            } else {
                model.getDependencies().removeIf(CommonTab::isHmi);
                final Dependency dependency = new Dependency();
                dependency.setGroupId("org.marid");
                dependency.setArtifactId("marid-runtime");
                dependency.setVersion("${marid.runtime.version}");
                model.getDependencies().add(dependency);
                return true;
            }
        }
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
        final Model model = new Model();
        model.setOrganization(new Organization());
        model.setName(getName());
        model.setArtifactId(getName());
        model.setGroupId("org.myproject");
        model.setVersion("1.0-SNAPSHOT");
        return model;
    }

    private ObservableList<Pair<Path, BeanFile>> loadBeanFiles() {
        try (final Stream<Path> stream = Files.walk(beansDirectory)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .map(p -> {
                        try {
                            return Pair.of(p, MaridBeanDefinitionLoader.load(p));
                        } catch (Exception x) {
                            log(WARNING, "Unable to load {0}", x, p);
                            return Pair.of(p, new BeanFile());
                        }
                    })
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        } catch (IOException x) {
            log(WARNING, "Unable to load bean files", x);
        } catch (Exception x) {
            log(SEVERE, "Unknown error", x);
        }
        return FXCollections.observableArrayList();
    }

    public ObservableList<Pair<Path, BeanFile>> getBeanFiles() {
        return beanFiles;
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

    public Path getSrcMainResources() {
        return srcMainResources;
    }

    public Path getTarget() {
        return target;
    }

    public String getName() {
        return path.getFileName().toString();
    }

    @Nonnull
    @Override
    public Logger logger() {
        return logger;
    }

    public Optional<Class<?>> getClass(String type) {
        return cacheEntry.getClass(type);
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
        } catch (Exception x) {
            log(WARNING, "Unable to create file structure", x);
        }
    }

    private void savePomFile() {
        try (final OutputStream os = Files.newOutputStream(pomFile)) {
            final MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(os, model);
        } catch (IOException x) {
            log(WARNING, "Unable to save {0}", x, pomFile);
        }
    }

    private void saveBeanFiles() {
        try {
            FileUtils.cleanDirectory(getBeansDirectory().toFile());
        } catch (IOException x) {
            log(WARNING, "Unable to clean beans directory", x);
            return;
        }
        for (final Pair<Path, BeanFile> entry : beanFiles) {
            try {
                Files.createDirectories(entry.getKey().getParent());
                MaridBeanDefinitionSaver.write(entry.getKey(), entry.getValue());
            } catch (Exception x) {
                log(WARNING, "Unable to save {0}", x, entry.getKey());
            }
        }
    }

    public void save() {
        createFileStructure();
        savePomFile();
        saveBeanFiles();
    }

    void delete() {
        try {
            cacheEntry.close();
            FileUtils.deleteDirectory(path.toFile());
        } catch (Exception x) {
            log(WARNING, "Unable to delete {0}", x, getName());
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
