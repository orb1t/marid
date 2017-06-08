/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.structure;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.printer.PrettyPrinter;
import org.apache.commons.io.FilenameUtils;
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
        final ProjectProfile profile = projectManager.getProfile(renamedEvent.getTarget()).orElse(null);
        if (profile == null) {
            return;
        }
        final Path javaBase = profile.getJavaBaseDir(renamedEvent.getTarget());
        if (javaBase == null) {
            return;
        }
        final Path path = javaBase.relativize(renamedEvent.getTarget());
        final String pkg = stream(path.getParent().spliterator(), false).map(Path::toString).collect(joining("."));
        final AtomicBoolean updated = new AtomicBoolean();
        try {
            final CompilationUnit compilationUnit = JavaParser.parse(renamedEvent.getTarget());
            compilationUnit.getPackageDeclaration().ifPresent(packageDeclaration -> {
                final Name name = packageDeclaration.getName();
                if (!pkg.equals(name.getIdentifier())) {
                    packageDeclaration.setName(pkg);
                    updated.set(true);
                }
            });
            compilationUnit.getTypes().stream()
                    .filter(TypeDeclaration::isTopLevelType)
                    .findAny()
                    .ifPresent(t -> {
                        t.setName(FilenameUtils.getBaseName(path.getFileName().toString()));
                        updated.set(true);
                    });
            if (updated.get()) {
                final String source = prettyPrinter.print(compilationUnit);
                Files.write(renamedEvent.getTarget(), source.getBytes(UTF_8));
            }
        } catch (Exception x) {
            log(WARNING, "Unable to parse or save {0}", x, renamedEvent.getTarget());
        }
    }
}
