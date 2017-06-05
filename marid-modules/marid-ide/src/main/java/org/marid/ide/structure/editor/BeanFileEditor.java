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

package org.marid.ide.structure.editor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithPublicModifier;
import javafx.scene.Node;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanEditorConfiguration;
import org.marid.dependant.beaneditor.BeanEditorParam;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectManager;
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
public class BeanFileEditor extends AbstractFileEditor<ProjectProfile> {

    private final ProjectManager projectManager;
    private final IdeDependants dependants;

    @Autowired
    public BeanFileEditor(@Qualifier("java") PathMatcher javaPathMatcher,
                          ProjectManager projectManager,
                          IdeDependants dependants) {
        super(javaPathMatcher);
        this.projectManager = projectManager;
        this.dependants = dependants;
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
    protected ProjectProfile editorContext(@Nonnull Path path) {
        try {
            final ProjectProfile profile = projectManager.getProfile(path).orElse(null);
            if (profile == null) {
                return null;
            }
            final CompilationUnit compilationUnit = JavaParser.parse(path);
            if (compilationUnit.getTypes().isEmpty()) {
                return null;
            }
            if (compilationUnit.getTypes().stream()
                    .filter(ClassOrInterfaceDeclaration.class::isInstance)
                    .map(ClassOrInterfaceDeclaration.class::cast)
                    .filter(c -> !c.isInterface())
                    .filter(TypeDeclaration::isTopLevelType)
                    .filter(NodeWithPublicModifier::isPublic)
                    .filter(c -> c.isAnnotationPresent(Generated.class))
                    .anyMatch(c -> !c.isFinal())) {
                return profile;
            } else {
                return null;
            }
        } catch (Exception x) {
            log(WARNING, "Unable to parse {0}", x, path);
            return null;
        }
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull ProjectProfile context) {
        dependants.start(BeanEditorConfiguration.class, new BeanEditorParam(context, new TextFile(file)), c -> {});
    }
}
