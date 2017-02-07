/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.jfx.controls.IdeShapes.fileNode;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class BeanEditorTab extends IdeTab {

    public final ProjectProfile profile;
    public final BeanFile file;

    @Autowired
    public BeanEditorTab(ProjectProfile profile, BeanListTable table, BeanFile file) {
        super(new MaridScrollPane(table), createStringBinding(file::getFilePath, file.path), () -> fileNode(file, 16));
        this.profile = profile;
        this.file = file;
        addNodeObservables(file.path);
    }
}
