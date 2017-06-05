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
import org.marid.ide.common.IdeShapes;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTab extends IdeTab {

    @Autowired
    public BeanEditorTab(BeanEditor editor, TextFile javaFile, ProjectManager projectManager) {
        super(editor, Bindings.createStringBinding(() -> {
            final Path relativePath = projectManager.getProfilesDir().relativize(javaFile.getPath());
            return relativePath.toString();
        }, javaFile.path), () -> IdeShapes.javaFile(javaFile.hashCode(), 16));
        addNodeObservables(javaFile.path);
    }
}
