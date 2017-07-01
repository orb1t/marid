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

package org.marid.ide.structure;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.printer.PrettyPrinter;
import org.apache.commons.io.FilenameUtils;
import org.marid.ide.event.TextFileAddedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Lazy(false)
@Service
public class JavaTextFileUpdater {

    private final PrettyPrinter prettyPrinter;
    private final ProjectManager projectManager;

    @Autowired
    public JavaTextFileUpdater(PrettyPrinter prettyPrinter, ProjectManager projectManager) {
        this.prettyPrinter = prettyPrinter;
        this.projectManager = projectManager;
    }

    @EventListener
    public void onRename(TextFileMovedEvent renamedEvent) {
        process(renamedEvent.getTarget());
    }

    @EventListener
    public void onAdd(TextFileAddedEvent addedEvent) {
        process(addedEvent.getSource());
    }

    private void process(Path file) {
        final ProjectProfile profile = projectManager.getProfile(file).orElse(null);
        if (profile == null) {
            return;
        }
        final Path javaBase = profile.getJavaBaseDir(file);
        if (javaBase == null) {
            return;
        }
        final Path path = javaBase.relativize(file);
        final String pkg = stream(path.getParent().spliterator(), false).map(Path::toString).collect(joining("."));
        final AtomicBoolean updated = new AtomicBoolean();
        try {
            final CompilationUnit compilationUnit = JavaParser.parse(file);

            compilationUnit.getPackageDeclaration()
                    .map(d -> {
                        final Name name = d.getName();
                        if (!pkg.equals(name.getIdentifier())) {
                            d.setName(pkg);
                            updated.set(true);
                        }
                        return d;
                    })
                    .orElseGet(() -> {
                        final PackageDeclaration d = new PackageDeclaration(new Name(pkg));
                        compilationUnit.setPackageDeclaration(d);
                        updated.set(true);
                        return d;
                    });

            compilationUnit.getTypes().stream()
                    .filter(TypeDeclaration::isTopLevelType)
                    .findFirst()
                    .ifPresent(t -> {
                        final String name = FilenameUtils.getBaseName(path.getFileName().toString());
                        if (!t.getNameAsString().equals(name)) {
                            t.setName(name);
                            updated.set(true);
                        }
                    });

            if (updated.get()) {
                final String source = prettyPrinter.print(compilationUnit);
                Files.write(file, source.getBytes(UTF_8));
                log(INFO, "Updated {0}", file);
            }
        } catch (Exception x) {
            log(WARNING, "Unable to parse or save {0}", x, file);
        }
    }
}
