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

package org.marid.ide.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.PrettyPrintVisitor;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.ide.project.ProjectProfile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class MainJavaClass {

    public final Path relativePath;
    public final CompilationUnit compilationUnit;

    public MainJavaClass(Path relativePath, CompilationUnit compilationUnit) {
        this.relativePath = relativePath;
        this.compilationUnit = compilationUnit;
    }

    public MainJavaClass(ProjectProfile profile, Path path) {
        relativePath = profile.getSrcMainJava().relativize(path);
        try {
            compilationUnit = JavaParser.parse(path);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    public void save(ProjectProfile profile) {
        final Path path = profile.getSrcMainJava().resolve(relativePath);
        final Path dir = path.getParent();
        try {
            Files.createDirectories(dir);

            final PrettyPrintVisitor visitor = new PrettyPrintVisitor(build(new PrettyPrinterConfiguration(), c -> {
                c.setEndOfLineCharacter("\n");
                c.setIndent(" ");
                c.setPrintComments(true);
            }));
            compilationUnit.accept(visitor, null);
            Files.write(path, visitor.getSource().getBytes(UTF_8));
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
