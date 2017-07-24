/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.project;

import com.google.common.io.MoreFiles;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.ide.model.BeanFile;
import org.marid.ide.types.BeanCache;
import org.marid.misc.Urls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.util.EnumSet.allOf;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toMap;
import static org.marid.ide.model.BeanDataNormalizer.registerNormalizer;
import static org.marid.ide.project.ProjectFileType.*;
import static org.marid.logging.Log.log;
import static org.marid.misc.Builder.build;
import static org.marid.misc.Calls.call;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectProfile {

    private final Model model;
    private final Path path;
    private final EnumMap<ProjectFileType, Path> paths;
    private final Logger logger;
    private final BooleanProperty enabled;
    private final BeanFile beanFile;
    private final Queue<WeakReference<Consumer<ProjectProfile>>> onUpdate = new ConcurrentLinkedQueue<>();

    private URLClassLoader classLoader;
    private BeanCache beanCache;

    ProjectProfile(Path profilesDir, String name) {
        path = profilesDir.resolve(name);
        paths = allOf(ProjectFileType.class).stream().collect(toMap(t -> t, t -> call(() -> {
            final Path p = t.toFull(path);
            if (!t.relative.startsWith("target")) {
                if (t.isDirectory()) {
                    Files.createDirectories(p);
                } else {
                    Files.createDirectories(p.getParent());
                    if (!Files.isRegularFile(p)) {
                        Files.createFile(p);
                    }
                }
            }
            return p;
        }), (v1, v2) -> v2, () -> new EnumMap<>(ProjectFileType.class)));
        logger = Logger.getLogger(getName());
        model = loadModel();
        model.setModelVersion("4.0.0");
        beanFile = build(new BeanFile(), f -> {
            registerNormalizer(f);
            f.load(this);
        });
        init();
        enabled = new SimpleBooleanProperty(true);
        enabled.addListener((o, oV, nV) -> {
            if (nV && !oV) {
                updateClassLoader();
            }
        });
    }

    private void init() {
        updateClassLoader();
        if (model.getProfiles().stream().noneMatch(p -> "conf".equals(p.getId()))) {
            final Profile profile = new Profile();
            profile.setId("conf");
            model.getProfiles().add(profile);
        }
    }

    public Path get(ProjectFileType type) {
        return paths.get(type);
    }

    private Model loadModel() {
        try (final InputStream is = Files.newInputStream(get(POM))) {
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

    public String getName() {
        return path.getFileName().toString();
    }

    public Path getJavaBaseDir(Path javaFile) {
        if (javaFile.startsWith(get(SRC_MAIN_JAVA))) {
            return get(SRC_MAIN_JAVA);
        } else {
            return null;
        }
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    private void savePomFile() {
        try (final OutputStream os = Files.newOutputStream(get(POM))) {
            final MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(os, model);
        } catch (IOException x) {
            log(logger, WARNING, "Unable to save {0}", x, get(POM));
        }
    }

    public void save() {
        savePomFile();
        beanFile.save(this);
    }

    public BeanFile getBeanFile() {
        return beanFile;
    }

    void delete() {
        try {
            MoreFiles.deleteRecursively(path);
        } catch (Exception x) {
            log(logger, WARNING, "Unable to delete {0}", x, getName());
        }
    }

    private void updateClassLoader() {
        try (final URLClassLoader old = classLoader; final BeanCache oldCache = beanCache) {
            final URL[] urls = Urls.classpath(get(TARGET_LIB), get(TARGET_CLASSES));
            final ClassLoader parent = Thread.currentThread().getContextClassLoader();
            classLoader = new URLClassLoader(urls, parent);
            beanCache = new BeanCache(beanFile.beans, classLoader);
            onUpdate.removeIf(ref -> {
                final Consumer<ProjectProfile> c = ref.get();
                if (c == null) {
                    return true;
                } else {
                    c.accept(this);
                    return false;
                }
            });
        } catch (Exception x) {
            log(logger, WARNING, "Unable to close class loader", x);
        }
    }

    public BeanCache getBeanCache() {
        return beanCache;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void addOnUpdate(Consumer<ProjectProfile> listener) {
        onUpdate.removeIf(ref -> ref.get() == null);
        onUpdate.add(new WeakReference<>(listener));
    }

    public void removeOnUpdate(Consumer<ProjectProfile> listener) {
        onUpdate.removeIf(ref -> {
            final Consumer<ProjectProfile> c = ref.get();
            return c == null || c.equals(listener);
        });
        onUpdate.remove(new WeakReference<>(listener));
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
