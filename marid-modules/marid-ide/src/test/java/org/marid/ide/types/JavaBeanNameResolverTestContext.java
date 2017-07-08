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

package org.marid.ide.types;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.google.common.io.MoreFiles;
import org.marid.ide.common.Directories;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static com.github.javaparser.ast.Modifier.PUBLIC;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class JavaBeanNameResolverTestContext {

    @Bean
    public Path tempDirectory() throws IOException {
        return Files.createTempDirectory("marid");
    }

    @Bean
    public Closeable tempDirectoryCleaner(Path tempDirectory) {
        return () -> MoreFiles.deleteRecursively(tempDirectory);
    }

    @Bean
    public Directories directories(Path tempDirectory) {
        return new Directories() {

            @Override
            public Path getUserHome() {
                return tempDirectory;
            }

            @Override
            public Path getMarid() {
                return tempDirectory.resolve("marid");
            }

            @Override
            public Path getProfiles() {
                return getMarid().resolve("profiles");
            }

            @Override
            public Path getRepo() {
                return getMarid().resolve("repo");
            }
        };
    }

    @Bean
    public PrettyPrinter prettyPrinter() {
        return new PrettyPrinter(new PrettyPrinterConfiguration()
                .setIndent("  ")
                .setEndOfLineCharacter("\n")
        );
    }

    @Bean
    public Path javaFileA(Directories directories) throws IOException {
        final Path profilesDir = directories.getProfiles();
        final Path profileDir = profilesDir.resolve("test");

        final Path a = profileDir
                .resolve("src")
                .resolve("main")
                .resolve("java")
                .resolve("org")
                .resolve("marid")
                .resolve("test")
                .resolve("A.java");

        Files.createDirectories(a.getParent());

        return a;
    }

    @Bean
    public ProjectManager manager(Directories directories, PrettyPrinter printer, Path javaFileA) throws Exception {
        final Path targetLib = directories.getProfiles().resolve("test").resolve("target").resolve("lib");
        Files.createDirectories(targetLib);

        try (final Scanner scanner = new Scanner(ClassLoader.getSystemResourceAsStream("jars/jars.lst"), "UTF-8")) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                final String[] parts = line.split(":");
                if (parts.length > 2) {
                    final String jar = parts[1] + ".jar";
                    try (final InputStream stream = ClassLoader.getSystemResourceAsStream("jars/" + jar)) {
                        Files.copy(stream, targetLib.resolve(jar), REPLACE_EXISTING);
                    }
                }
            }
            log(INFO, "Copied {0} jars", Files.list(targetLib).count());
        }

        final ProjectManager manager = new ProjectManager(directories);

        final CompilationUnit compilationUnit = new CompilationUnit("org.marid.test")
                .addClass("A", PUBLIC)
                .addAnnotation(new MarkerAnnotationExpr(Component.class.getName()))

                .addMethod("bean1", PUBLIC)
                .setType(JavaParser.parseType("java.util.function.Function<Integer, Long>"))
                .setBody(JavaParser.parseBlock("{return a -> (long) a.intValue();}"))
                .addAnnotation(new MarkerAnnotationExpr(Bean.class.getName()))
                .getAncestorOfType(ClassOrInterfaceDeclaration.class).orElse(null)

                .addMethod("bean2", PUBLIC)
                .setType(JavaParser.parseType("java.util.function.Function<Long, Long>"))
                .setBody(JavaParser.parseBlock("{return a -> a;}"))
                .addAnnotation(new MarkerAnnotationExpr(Bean.class.getName()))
                .getAncestorOfType(ClassOrInterfaceDeclaration.class).orElse(null)

                .getAncestorOfType(CompilationUnit.class).orElse(null);

        Files.write(javaFileA, printer.print(compilationUnit).getBytes(UTF_8));

        manager.getProfiles().stream()
                .filter(p -> "test".equals(p.getName()))
                .forEach(ProjectProfile::save);

        return manager;
    }

    @Bean
    public JavaBeanNameResolver beanNameResolver(ProjectManager manager) {
        return new JavaBeanNameResolver(manager);
    }
}
