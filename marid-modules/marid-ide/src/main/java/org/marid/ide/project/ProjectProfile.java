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
import javafx.collections.ObservableList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.jfx.beans.FxList;
import org.marid.logging.LogSupport;
import org.marid.spring.xml.*;
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;
import static org.marid.util.Reflections.parameterName;
import static org.springframework.core.ResolvableType.*;

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
    final FxList<BeanFile> beanFiles;
    final ProjectCacheEntry cacheEntry;

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
    }

    public URLClassLoader getClassLoader() {
        return cacheEntry.getClassLoader();
    }

    public boolean containsBean(String name) {
        return beanFiles.stream().anyMatch(f -> f.allBeans().anyMatch(b -> name.equals(b.getName())));
    }

    public String generateBeanName(String name) {
        while (containsBean(name)) {
            name += "_new";
        }
        return name;
    }

    void update() throws Exception {
        cacheEntry.update();
        Platform.runLater(ResolvableType::clearCache);
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

    private FxList<BeanFile> loadBeanFiles() {
        final FxList<BeanFile> list = new FxList<>(BeanFile::observables);
        try (final Stream<Path> stream = Files.walk(beansDirectory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .map(p -> {
                        final BeanFile template = BeanFile.beanFile(getBeansDirectory(), p);
                        try {
                            final BeanFile file = MaridBeanDefinitionLoader.load(p);
                            file.path.setAll(template.path);
                            return file;
                        } catch (Exception x) {
                            log(WARNING, "Unable to load {0}", x, p);
                        }
                        return template;
                    })
                    .forEach(list::add);
        } catch (IOException x) {
            log(WARNING, "Unable to load bean files", x);
        } catch (Exception x) {
            log(SEVERE, "Unknown error", x);
        }
        return list;
    }

    public FxList<BeanFile> getBeanFiles() {
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
        final Path base = getBeansDirectory();
        for (final BeanFile file : beanFiles) {
            final Path path = file.path(base);
            try {
                Files.createDirectories(path.getParent());
                MaridBeanDefinitionSaver.write(path, file);
            } catch (Exception x) {
                log(WARNING, "Unable to save {0}", x, path);
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

    public Optional<Class<?>> getClass(BeanData data) {
        if (data.isFactoryBean()) {
            return getConstructor(data).map(e -> ((Method) e).getReturnType());
        } else {
            return getClass(data.type.get());
        }
    }

    public Stream<? extends Executable> getConstructors(BeanData data) {
        if (data.isFactoryBean()) {
            return getBeanFiles().stream()
                    .flatMap(BeanFile::allBeans)
                    .filter(b -> Objects.equals(data.getFactoryBean(), b.getName()))
                    .map(this::getClass)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(t -> of(t.getMethods()))
                    .filter(m -> m.getReturnType() != void.class)
                    .filter(m -> m.getName().equals(data.getFactoryMethod()))
                    .sorted(comparingInt(Method::getParameterCount));
        } else {
            return getClass(data)
                    .map(c -> of(c.getConstructors()).sorted(comparingInt(Constructor::getParameterCount)))
                    .orElseGet(Stream::empty);
        }
    }

    public Optional<? extends Executable> getConstructor(BeanData data) {
        final List<? extends Executable> executables = getConstructors(data).collect(toList());
        switch (executables.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(executables.get(0));
            default:
                final Class<?>[] types = data.beanArgs.stream()
                        .map(a -> getClass(a.type.get()).orElse(Object.class))
                        .toArray(Class<?>[]::new);
                return executables.stream().filter(m -> Arrays.equals(types, m.getParameterTypes())).findFirst();
        }
    }

    public void updateBeanDataConstructorArgs(BeanData data, Parameter[] parameters) {
        final List<BeanArg> args = of(parameters)
                .map(p -> {
                    final Optional<BeanArg> found = data.beanArgs.stream()
                            .filter(a -> parameterName(p).equals(a.getName()))
                            .findFirst();
                    if (found.isPresent()) {
                        found.get().type.set(p.getType().getName());
                        return found.get();
                    } else {
                        final BeanArg arg = new BeanArg();
                        arg.name.set(parameterName(p));
                        arg.type.set(p.getType().getName());
                        return arg;
                    }
                })
                .collect(toList());
        data.beanArgs.setAll(args);
    }

    public void updateBeanData(BeanData data) {
        final Class<?> type = getClass(data).orElse(null);
        if (type == null) {
            return;
        }
        final List<Executable> executables = getConstructors(data).collect(toList());
        data.constructors.setAll(executables);
        if (!executables.isEmpty()) {
            if (executables.size() == 1) {
                updateBeanDataConstructorArgs(data, executables.get(0).getParameters());
            } else {
                final Optional<? extends Executable> executable = getConstructor(data);
                executable.ifPresent(e -> updateBeanDataConstructorArgs(data, e.getParameters()));
            }
        }

        final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(data).collect(toList());
        final Map<String, BeanProp> pmap = data.properties.stream().collect(toMap(e -> e.name.get(), e -> e));
        data.properties.clear();
        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            final BeanProp prop = pmap.computeIfAbsent(propertyDescriptor.getName(), n -> {
                final BeanProp property = new BeanProp();
                property.name.set(n);
                return property;
            });
            data.properties.add(prop);
        }
    }

    public Stream<PropertyDescriptor> getPropertyDescriptors(BeanData data) {
        final Class<?> type = getClass(data).orElse(Object.class);
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(type);
            return of(beanInfo.getPropertyDescriptors())
                    .filter(d -> d.getWriteMethod() != null);
        } catch (IntrospectionException x) {
            return Stream.empty();
        }
    }

    public ResolvableType getType(BeanData beanData) {
        if (beanData.isFactoryBean()) {
            return getConstructor(beanData).map(e -> forMethodReturnType((Method) e)).orElse(NONE);
        } else {
            return getClass(beanData.type.get()).map(ResolvableType::forClass).orElse(NONE);
        }
    }

    public ResolvableType getArgType(BeanData beanData, String name) {
        return getConstructor(beanData)
                .flatMap(e -> of(e.getParameters()).filter(p -> parameterName(p).equals(name)).findAny())
                .map(p -> ResolvableType.forType(p.getParameterizedType()))
                .orElse(NONE);
    }

    public ResolvableType getPropType(BeanData beanData, String name) {
        return getPropertyDescriptors(beanData)
                .filter(d -> d.getName().equals(name))
                .findAny()
                .map(p -> forMethodParameter(p.getWriteMethod(), 0))
                .orElse(NONE);
    }
}
