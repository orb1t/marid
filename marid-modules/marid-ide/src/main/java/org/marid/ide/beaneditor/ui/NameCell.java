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

package org.marid.ide.beaneditor.ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.ImageView;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.marid.ide.beaneditor.BeanTreeConstants;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import java.nio.file.Path;

import static de.jensd.fx.glyphs.octicons.OctIcon.FILE_ADD;
import static de.jensd.fx.glyphs.octicons.OctIcon.FILE_DIRECTORY_CREATE;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameCell extends TextFieldTreeTableCell<Object, String> implements L10nSupport, LogSupport, BeanTreeConstants {

    private final TreeTableColumn<Object, String> column;

    public NameCell(TreeTableColumn<Object, String> column) {
        this.column = column;
        setAlignment(Pos.CENTER_LEFT);
        setConverter(new DefaultStringConverter());
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        final TreeItem<Object> treeItem = getTreeTableRow() == null ? null : getTreeTableRow().getTreeItem();
        if (empty || item == null || treeItem == null) {
            setContextMenu(null);
        } else {
            final Object value = treeItem.getValue();
            final ContextMenu contextMenu = new ContextMenu();
            if (value instanceof Path) {
                final Path path = (Path) value;
                if (!path.getFileName().toString().endsWith(".xml")) {
                    itemAddSubdirectory(contextMenu, path, treeItem);
                    itemAddBeanFile(contextMenu, path, treeItem);
                }
            } else if (value instanceof ProjectProfile) {
                final ProjectProfile profile = (ProjectProfile) value;
                itemAddSubdirectory(contextMenu, profile.getBeansDirectory(), treeItem);
                itemAddBeanFile(contextMenu, profile.getBeansDirectory(), treeItem);
            }
            setContextMenu(contextMenu);
        }
    }

    @Override
    public void startEdit() {
        final TreeTableRow<Object> row = getTreeTableRow();
        final TreeItem<Object> item = row.getTreeItem();
        if (item.getValue() instanceof BeanData || item.getValue() instanceof Path) {
            super.startEdit();
        }
    }

    private void itemAddSubdirectory(ContextMenu menu, Path parent, TreeItem<Object> parentItem) {
        menu.getItems().add(build(new MenuItem(s("Add subdirectory"), glyphIcon(FILE_DIRECTORY_CREATE)), i -> {
            i.setOnAction(event -> {
                final Path newPath = parent.resolve("newFolder");
                final TreeItem<Object> newFolderItem = new TreeItem<>(newPath, new ImageView(DIR));
                parentItem.getChildren().add(newFolderItem);
            });
        }));
    }

    private void itemAddBeanFile(ContextMenu menu, Path parent, TreeItem<Object> parentItem) {
        menu.getItems().add(build(new MenuItem(s("Add beans file"), glyphIcon(FILE_ADD)), i -> {
            i.setOnAction(event -> {
                final Path newPath = parent.resolve("newBeansFile.xml");
                final TreeItem<Object> newItem = new TreeItem<>(newPath, new ImageView(DIR));
                parentItem.getChildren().add(newItem);
            });
        }));
    }

    @Override
    public void commitEdit(String newValue) {
        if (StringUtils.isBlank(newValue)) {
            cancelEdit();
            return;
        }
        final TreeTableRow<Object> row = getTreeTableRow();
        final TreeItem<Object> item = row.getTreeItem();
        if (item.getValue() instanceof BeanData) {
            super.commitEdit(newValue);
        } else if (item.getValue() instanceof Path) {
            final Path path = (Path) item.getValue();
            final Path newPath = path.getParent().resolve(newValue);
            item.setValue(newPath);
            super.commitEdit(newValue);
        } else {
            cancelEdit();
        }
    }
}
