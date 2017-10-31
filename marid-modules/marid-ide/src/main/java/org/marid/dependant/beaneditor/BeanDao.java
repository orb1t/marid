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

package org.marid.dependant.beaneditor;

import org.marid.ide.project.ProjectProfile;
import org.marid.io.PathMatchers;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.nio.file.Files.find;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.project.ProjectFileType.TARGET_CLASSES;
import static org.marid.ide.project.ProjectFileType.TARGET_LIB;
import static org.marid.logging.Log.log;
import static org.springframework.asm.ClassReader.*;
import static org.springframework.asm.Opcodes.ASM6;

@Repository
public class BeanDao implements AutoCloseable {

    private final ProjectProfile profile;
    private final ConcurrentLinkedQueue<Class<?>> publicClasses = new ConcurrentLinkedQueue<>();

    @Autowired
    public BeanDao(ProjectProfile profile) {
        (this.profile = profile).addOnUpdate(this::onUpdate);
        onUpdate(profile);
    }

    private void onUpdate(ProjectProfile profile) {
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
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                if (!name.contains("$") && (access & Opcodes.ACC_PUBLIC) != 0) {
                    classNames.add(name.replace('/', '.'));
                }
            }
        }, SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES));
        publicClasses.clear();
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
        log(INFO, "{0} Public classes updated: {1}", profile, publicClasses.size());
    }

    public Collection<Class<?>> publicClasses() {
        return Collections.unmodifiableCollection(publicClasses);
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

    @Override
    public void close() {
        profile.removeOnUpdate(this::onUpdate);
    }
}
