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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProjectProfileUtils {

    BiPredicate<Path, BasicFileAttributes> JAVA_FILE_FILTER = (p, a) -> p.getFileName().toString().endsWith(".java");

    Predicate<TypeDeclaration<?>> COMPONENT = type -> type.isAnnotationPresent(Component.class);
    Predicate<TypeDeclaration<?>> CONFIGURATION = type -> type.isAnnotationPresent(Configuration.class);
    Predicate<TypeDeclaration<?>> REPOSITORY = type -> type.isAnnotationPresent(Repository.class);
    Predicate<TypeDeclaration<?>> SERVICE = type -> type.isAnnotationPresent(Service.class);
    Predicate<TypeDeclaration<?>> CONTROLLER = type -> type.isAnnotationPresent(Controller.class);

    Predicate<TypeDeclaration<?>> SCANNED_TYPES = COMPONENT.or(CONFIGURATION).or(REPOSITORY).or(SERVICE).or(CONTROLLER);

    static void saveBeanFiles(ProjectProfile profile) {
        final Path listFile = profile.getMetaDirectory().resolve("bean-classes.lst");
        try (
                final Stream<Path> javaFiles = Files.find(profile.getSrcMainJava(), 255, JAVA_FILE_FILTER);
                final BufferedWriter writer = Files.newBufferedWriter(listFile)
        ) {
            final List<Path> files = javaFiles.collect(Collectors.toList());
            for (final Path file : files) {
                try {
                    final CompilationUnit unit = JavaParser.parse(file, UTF_8);
                    if (unit.getTypes().stream().anyMatch(SCANNED_TYPES)) {
                        final Path baseDir = profile.getJavaBaseDir(file);
                        final Path relative = baseDir.relativize(file);
                        final String className = IntStream.range(0, relative.getNameCount())
                                .mapToObj(i -> relative.getName(i).toString())
                                .collect(Collectors.joining(".")).replace(".java", "");
                        writer.write(className);
                        writer.newLine();
                    }
                } catch (Exception x) {
                    log(WARNING, "Unable to parse {0}", x, file);
                }
            }
        } catch (IOException x) {
            log(WARNING, "Unable to save bean-classes.lst", x);
        }
    }
}
