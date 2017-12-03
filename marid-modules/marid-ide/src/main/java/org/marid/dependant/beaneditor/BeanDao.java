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

package org.marid.dependant.beaneditor;

import javafx.beans.InvalidationListener;
import org.marid.ide.project.ProjectProfile;
import org.marid.io.PathMatchers;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.nio.file.Files.find;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.project.ProjectFileType.TARGET_CLASSES;
import static org.marid.ide.project.ProjectFileType.TARGET_LIB;
import static org.marid.logging.Log.log;
import static org.springframework.asm.ClassReader.*;
import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ASM6;

@Repository
public class BeanDao {

  private final ProjectProfile profile;
  private final Set<Class<?>> publicClasses = new LinkedHashSet<>();
  private final AtomicBoolean dirty = new AtomicBoolean(true);
  private final InvalidationListener listener = o -> dirty.set(true);

  @Autowired
  public BeanDao(ProjectProfile profile) {
    (this.profile = profile).addListener(listener);
    listener.invalidated(profile);
  }

  private ConcurrentLinkedQueue<Class<?>> classes() {
    final ConcurrentLinkedQueue<Class<?>> publicClasses = new ConcurrentLinkedQueue<>();
    final Path lib = profile.get(TARGET_LIB);
    final ConcurrentLinkedQueue<ClassReader> classReaders = new ConcurrentLinkedQueue<>();
    try (final DirectoryStream<Path> libStream = Files.newDirectoryStream(lib, "*.jar")) {
      for (final Path jar : libStream) {
        try (final FileSystem fileSystem = FileSystems.newFileSystem(jar, getClass().getClassLoader())) {
          for (final Path root : fileSystem.getRootDirectories()) {
            try {
              fillClassReaders(root, classReaders);
            } catch (Exception x) {
              log(WARNING, "Unable to process {0}", x, root);
            }
          }
        }
      }
    } catch (Exception x) {
      log(WARNING, "Unable to enumerate jar files", x);
    }
    try {
      fillClassReaders(profile.get(TARGET_CLASSES), classReaders);
    } catch (Exception x) {
      log(WARNING, "Unable to enumerate class files", x);
    }
    final ConcurrentLinkedQueue<String> classNames = new ConcurrentLinkedQueue<>();
    classReaders.parallelStream().forEach(r -> r.accept(new ClassVisitor(ASM6) {
      @Override
      public void visit(int v, int access, String name, String signature, String superName, String[] interfaces) {
        if (!name.contains("$") && (access & ACC_PUBLIC) != 0) {
          classNames.add(name.replace('/', '.'));
        }
      }
    }, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES));
    classNames.parallelStream().forEach(c -> {
      try {
        final Class<?> type = Class.forName(c, false, profile.getClassLoader());
        if (type.getConstructors().length > 0) {
          publicClasses.add(type);
        } else if (Stream.of(type.getMethods()).anyMatch(m -> Modifier.isStatic(m.getModifiers()))) {
          publicClasses.add(type);
        } else if (Stream.of(type.getFields()).anyMatch(f -> Modifier.isStatic(f.getModifiers()))) {
          publicClasses.add(type);
        }
      } catch (NoClassDefFoundError | ClassNotFoundException x) {
        // skip
      } catch (Throwable x) {
        log(WARNING, "Unable to load {0}", x, c);
      }
    });
    return publicClasses;
  }

  public Collection<Class<?>> publicClasses() {
    if (dirty.compareAndSet(true, false)) {
      final ConcurrentLinkedQueue<Class<?>> classes = classes();
      publicClasses.clear();
      publicClasses.addAll(classes);
      log(INFO, "{0} Public classes updated: {1}", profile, publicClasses.size());
    }
    return publicClasses;
  }

  private void fillClassReaders(Path root, Collection<ClassReader> classReaders) throws Exception {
    try (final Stream<Path> classStream = find(root, MAX_VALUE, PathMatchers::isClassFile)) {
      classStream.parallel().forEach(path -> {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
          Files.copy(path, bos);
        } catch (IOException x) {
          throw new UncheckedIOException(x);
        }
        classReaders.add(new ClassReader(bos.toByteArray()));
      });
    }
  }

  @PreDestroy
  private void close() {
    profile.removeListener(listener);
  }
}
