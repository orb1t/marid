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
import javafx.application.Platform;
import org.apache.commons.lang3.ClassUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.jfx.beans.OOList;
import org.marid.misc.Urls;
import org.marid.spring.xml.BeanFile;
import org.marid.spring.xml.MaridBeanDefinitionLoader;
import org.marid.spring.xml.MaridBeanDefinitionSaver;
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.logging.Level.*;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;
import static org.marid.logging.Log.log;

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
    private final OOList<BeanFile> beanFiles;
    private volatile URLClassLoader classLoader;

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
        classLoader = classLoader();
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    private URLClassLoader classLoader() {
        try (final Stream<URL> urls = Urls.classpath(target.resolve("lib"), target.resolve("classes"))) {
            return new URLClassLoader(urls.toArray(URL[]::new));
        }
    }

    public boolean containsBean(String name) {
        return beanFiles.stream().anyMatch(f -> f.beans.stream().anyMatch(b -> name.equals(b.getName())));
    }

    public String generateBeanName(String name) {
        while (containsBean(name)) {
            name += "_new";
        }
        return name;
    }

    private void close() {
        try (final URLClassLoader classLoader = this.classLoader) {
            log(logger, INFO, "Closing a class loader {0}", classLoader);
        } catch (IOException x) {
            log(logger, WARNING, "Class loader close error", x);
        }
    }

    void update() throws Exception {
        close();
        classLoader = classLoader();
        Platform.runLater(this::refresh);
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
        final Model model = new Model();
        model.setOrganization(new Organization());
        model.setName(getName());
        model.setArtifactId(getName());
        model.setGroupId("org.myproject");
        model.setVersion("1.0-SNAPSHOT");
        return model;
    }

    private OOList<BeanFile> loadBeanFiles() {
        final OOList<BeanFile> list = new OOList<>();
        try (final Stream<Path> stream = Files.walk(beansDirectory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .map(p -> {
                        final BeanFile template = BeanFile.beanFile(getBeansDirectory(), p);
                        try {
                            final BeanFile file = MaridBeanDefinitionLoader.load(p);
                            file.path.setAll(template.path);
                            return file;
                        } catch (Exception x) {
                            log(logger, WARNING, "Unable to load {0}", x, p);
                        }
                        return template;
                    })
                    .forEach(list::add);
        } catch (IOException x) {
            log(logger, WARNING, "Unable to load bean files", x);
        } catch (Exception x) {
            log(logger, SEVERE, "Unknown error", x);
        }
        return list;
    }

    public OOList<BeanFile> getBeanFiles() {
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
    public Logger logger() {
        return logger;
    }

    public Optional<Class<?>> getClass(String type) {
        try {
            return Optional.of(ClassUtils.getClass(classLoader, type, false));
        } catch (Exception x) {
            return Optional.empty();
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

    private void saveBeanFiles() {
        try {
            MoreFiles.deleteDirectoryContents(getBeansDirectory());
        } catch (IOException x) {
            log(logger, WARNING, "Unable to clean beans directory", x);
            return;
        }
        final Path base = getBeansDirectory();
        for (final BeanFile file : beanFiles) {
            final Path path = file.path(base);
            try {
                Files.createDirectories(path.getParent());
                MaridBeanDefinitionSaver.write(path, file);
            } catch (Exception x) {
                log(logger, WARNING, "Unable to save {0}", x, path);
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
            close();
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

    public void refresh() {
        final Set<Object> passed = Collections.newSetFromMap(new IdentityHashMap<>());
        ResolvableType.clearCache();
        getBeanFiles().forEach(f -> f.refresh(this, passed));
    }
}
