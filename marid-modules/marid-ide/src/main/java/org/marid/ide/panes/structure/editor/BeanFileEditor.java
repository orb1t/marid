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

package org.marid.ide.panes.structure.editor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithPublicModifier;
import javafx.scene.Node;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Generated;
import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileEditor extends AbstractFileEditor {

    @Autowired
    public BeanFileEditor(@Qualifier("java") PathMatcher javaPathMatcher) {
        super(javaPathMatcher);
    }

    @Override
    public void edit(@Nonnull ProjectProfile profile, @Nonnull Path file) {

    }

    @Nonnull
    @Override
    public String getName() {
        return "Bean File Editor";
    }

    @Nonnull
    @Override
    public Node getIcon() {
        return FontIcons.glyphIcon("M_APPS", 16);
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "bean";
    }

    @Override
    protected boolean isEditable(@Nonnull Path path, @Nonnull ProjectProfile profile) {
        try {
            final CompilationUnit compilationUnit = JavaParser.parse(path);
            return !compilationUnit.getTypes().isEmpty() && compilationUnit.getTypes().stream()
                    .filter(ClassOrInterfaceDeclaration.class::isInstance)
                    .map(ClassOrInterfaceDeclaration.class::cast)
                    .filter(c -> !c.isInterface())
                    .filter(TypeDeclaration::isTopLevelType)
                    .filter(NodeWithPublicModifier::isPublic)
                    .filter(c -> c.isAnnotationPresent(Generated.class))
                    .anyMatch(c -> !c.isFinal());
        } catch (Exception x) {
            log(WARNING, "Unable to parse {0}", x, path);
            return false;
        }
    }
}
