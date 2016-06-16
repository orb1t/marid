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

package org.marid.ide.panes.filebrowser;

import javafx.geometry.Orientation;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserPane extends BorderPane {

    final BeanFileBrowserTree tree;

    @Autowired
    public BeanFileBrowserPane(BeanFileBrowserTree tree) {
        setCenter(ScrollPanes.scrollPane(this.tree = tree));
        setLeft(new ToolbarBuilder()
                .add("Add file", M_ADD, event -> {
                })
                .add("Add directory", M_FOLDER, event -> {
                })
                .addSeparator()
                .add("Rename file/directory", O_DIFF_RENAMED, event -> {
                })
                .addSeparator()
                .add("Delete file/directory", O_TAG_REMOVE, event -> {
                })
                .build(toolBar -> toolBar.setOrientation(Orientation.VERTICAL)));
    }
}
