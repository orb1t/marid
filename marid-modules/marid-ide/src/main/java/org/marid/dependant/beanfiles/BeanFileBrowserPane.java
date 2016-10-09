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

package org.marid.dependant.beanfiles;

import javafx.geometry.Orientation;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.isEmpty;
import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserPane extends BorderPane {

    @Autowired
    public BeanFileBrowserPane(BeanFileBrowserActions actions, BeanFileBrowser tree) {
        setCenter(new MaridScrollPane(tree));
        setLeft(new ToolbarBuilder()
                .add("Add file", M_ADD, actions::onFileAdd)
                .addSeparator()
                .add("Rename file/directory", O_DIFF_RENAMED, actions::onRename, isEmpty(tree.getSelectionModel().getSelectedItems()))
                .addSeparator()
                .add("Delete file/directory", O_TAG_REMOVE, actions::onDelete, isEmpty(tree.getSelectionModel().getSelectedItems()))
                .addSeparator()
                .add("Bean editor...", M_EDIT, actions::launchBeanEditor, isEmpty(tree.getSelectionModel().getSelectedItems()))
                .build(toolBar -> toolBar.setOrientation(Orientation.VERTICAL)));
    }
}
