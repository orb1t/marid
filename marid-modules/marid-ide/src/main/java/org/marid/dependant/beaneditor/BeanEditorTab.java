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

package org.marid.dependant.beaneditor;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.HBox;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.marid.ide.common.IdeShapes.circle;
import static org.marid.ide.common.IdeShapes.javaFile;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTab extends IdeTab {

    @Autowired
    public BeanEditorTab(BeanEditorTable editor, TextFile javaFile, ProjectManager projectManager) {
        super(
                editor,
                Bindings.createStringBinding(() -> projectManager.getProfile(javaFile.getPath())
                        .map(p -> p.getJavaBaseDir(javaFile.getPath()))
                        .filter(Objects::nonNull)
                        .map(p -> p.relativize(javaFile.getPath()))
                        .map(p -> range(0, p.getNameCount()).mapToObj(p::getName).map(Path::toString).collect(joining(".")))
                        .orElseGet(() -> javaFile.getPath().toString()), javaFile.path),
                () -> {
                    final String profile = projectManager.getProfile(javaFile.getPath())
                            .map(ProjectProfile::getName)
                            .orElse("");
                    return new HBox(3, circle(profile.hashCode(), 16), javaFile(javaFile.hashCode(), 16));
                });
        addNodeObservables(javaFile.path);
    }
}
