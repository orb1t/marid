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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.logging.LogSupport;
import org.marid.misc.Calls;
import org.marid.spring.xml.MaridBeanDefinitionLoader;
import org.marid.spring.xml.MaridBeanDefinitionSaver;
import org.marid.spring.xml.data.BeanFile;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectProfile implements Observable, LogSupport {

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
    private final ObservableMap<Path, BeanFile> beanFiles;
    private final Map<String, Class<?>> classMap = new HashMap<>();
    private final List<InvalidationListener> invalidationListeners = new CopyOnWriteArrayList<>();

    private URLClassLoader classLoader;

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
        updateCache();
    }

    private void init() {
        if (model.getProfiles().stream().noneMatch(p -> "ui".equals(p.getId()))) {
            final Profile profile = new Profile();
            profile.setId("ui");
            model.getProfiles().add(profile);
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
        return new Model();
    }

    private ObservableMap<Path, BeanFile> loadBeanFiles() {
        try (final Stream<Path> stream = Files.walk(beansDirectory)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .collect(Collectors.toMap(p -> p, p -> {
                        try {
                            return MaridBeanDefinitionLoader.load(p);
                        } catch (Exception x) {
                            log(WARNING, "Unable to load {0}", x, p);
                            return new BeanFile();
                        }
                    }, (m1, m2) -> m2, FXCollections::observableHashMap));
        } catch (IOException x) {
            log(WARNING, "Unable to load bean files", x);
        } catch (Exception x) {
            log(SEVERE, "Unknown error", x);
        }
        return FXCollections.observableHashMap();
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        invalidationListeners.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        invalidationListeners.remove(invalidationListener);
    }

    public ObservableMap<Path, BeanFile> getBeanFiles() {
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

    private void fireInvalidated() {
        for (final InvalidationListener invalidationListener : invalidationListeners) {
            try {
                invalidationListener.invalidated(this);
            } catch (Exception x) {
                log(WARNING, "Unable to fire {0}", x, invalidationListener);
            }
        }
    }

    public void updateCache() {
        try (final URLClassLoader classLoader = this.classLoader) {
            classMap.clear();
        } catch (Exception x) {
            log(WARNING, "Unable to close class loader", x);
        }
        classLoader = classLoader();
        fireInvalidated();
    }

    public Class<?> getClass(String type) {
        return classMap.computeIfAbsent(type, t -> {
            try {
                return Class.forName(t, false, classLoader);
            } catch (Exception x) {
                log(WARNING, "Unable to load {0}", x, t);
                return Object.class;
            }
        });
    }

    public URLClassLoader classLoader() {
        final Path lib = target.resolve("lib");
        final List<URL> urls;
        if (Files.isDirectory(lib)) {
            final File[] files = lib.toFile().listFiles((dir, name) -> name.endsWith(".jar"));
            urls = Stream.of(files).map(f -> Calls.call(() -> f.toURI().toURL())).collect(Collectors.toList());
        } else {
            urls = new ArrayList<>();
        }
        if (Files.isDirectory(target.resolve("classes"))) {
            urls.add(Calls.call(() -> target.resolve("classes").toUri().toURL()));
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    private void createFileStructure() {
        try {
            for (final Path dir : asList(srcMainJava, beansDirectory, srcTestJava, srcTestResources)) {
                Files.createDirectories(dir);
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
        for (final Map.Entry<Path, BeanFile> entry : beanFiles.entrySet()) {
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
