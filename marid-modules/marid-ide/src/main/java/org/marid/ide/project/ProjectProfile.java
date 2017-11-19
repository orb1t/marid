/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.ide.project;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.marid.misc.Urls;
import org.marid.runtime.context.MaridDefaultContextListener;
import org.marid.runtime.context.MaridLogContextListener;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toMap;
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
  private final ConcurrentLinkedQueue<Consumer<ProjectProfile>> onUpdate = new ConcurrentLinkedQueue<>();

  private volatile URLClassLoader classLoader;

  ProjectProfile(Path profilesDir, String name) {
    path = profilesDir.resolve(name);
    paths = allOf(ProjectFileType.class).stream().collect(toMap(t -> t, t -> call(() -> {
      final Path p = t.toFull(path);
      if (!t.relative.startsWith("target")) {
        if (t.isDirectory()) {
          Files.createDirectories(p);
        } else {
          Files.createDirectories(p.getParent());
          if (!Files.isRegularFile(p) && !p.getFileName().toString().endsWith(".xml")) {
            Files.createFile(p);
          }
        }
      }
      return p;
    }), (v1, v2) -> v2, () -> new EnumMap<>(ProjectFileType.class)));
    logger = Logger.getLogger(getName());
    model = loadModel();
    model.setModelVersion("4.0.0");
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
  }

  public Path get(ProjectFileType type) {
    return paths.get(type);
  }

  private Model loadModel() {
    try (final InputStream is = Files.newInputStream(get(POM))) {
      final MavenXpp3Reader reader = new MavenXpp3Reader();
      return reader.read(is);
    } catch (NoSuchFileException x) {
      try {
        write(get(CONTEXT_LISTENERS), asList(
            MaridLogContextListener.class.getName(),
            MaridDefaultContextListener.class.getName()
        ), UTF_8);
      } catch (IOException ix) {
        log(logger, WARNING, "Unable to write default services", ix);
      }
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
  }

  void delete() {
    try {
      FileSystemUtils.deleteRecursively(path);
    } catch (Exception x) {
      log(logger, WARNING, "Unable to delete {0}", x, getName());
    }
  }

  private void updateClassLoader() {
    final URLClassLoader old = classLoader;
    try (old) {
      final URL[] urls = Urls.classpath(get(TARGET_LIB), get(TARGET_CLASSES));
      final ClassLoader parent = Thread.currentThread().getContextClassLoader();
      classLoader = new URLClassLoader(urls, parent);
      onUpdate.forEach(c -> {
        try {
          c.accept(this);
        } catch (Exception x) {
          log(WARNING, "Unable to call update trigger {0}", x, c);
        }
      });
    } catch (Exception x) {
      log(logger, WARNING, "Unable to close class loader", x);
    }
  }

  public URLClassLoader getClassLoader() {
    return classLoader;
  }

  public void addOnUpdate(Consumer<ProjectProfile> listener) {
    onUpdate.add(listener);
  }

  public void removeOnUpdate(Consumer<ProjectProfile> listener) {
    onUpdate.remove(listener);
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
