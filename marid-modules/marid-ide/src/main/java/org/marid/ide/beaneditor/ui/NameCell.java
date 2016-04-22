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

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.marid.ide.beaneditor.BeanEditor;
import org.marid.ide.beaneditor.BeanTreeConstants;
import org.marid.ide.beaneditor.BeanTreeUtils;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.Property;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.copy.Copies;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameCell extends TextFieldTreeTableCell<Object, String> implements L10nSupport, LogSupport, BeanTreeConstants {

    private static Copies<BeanEditor, TreeItem<Object>> copies;

    private final BeanEditor beanEditor;

    public NameCell(TreeTableColumn<Object, String> column, BeanEditor beanEditor) {
        this.beanEditor = beanEditor;
        copies = new Copies<>(beanEditor);
        updateTreeTableColumn(column);
        setAlignment(Pos.CENTER_LEFT);
        setConverter(new DefaultStringConverter());
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        final TreeItem<Object> treeItem = getTreeTableRow() == null ? null : getTreeTableRow().getTreeItem();
        setContextMenu(null);
        if (!empty && treeItem != null) {
            if (treeItem.getValue() instanceof Path) {
                setContextMenu(NameMenuFactory.menu(beanEditor, treeItem, (Path) treeItem.getValue()));
            } else if (treeItem.getValue() instanceof ProjectProfile) {
                setContextMenu(NameMenuFactory.menu(beanEditor, treeItem, (ProjectProfile) treeItem.getValue()));
            }
            setOnDragDetected(event -> copies.start(currentItem(event), BeanTreeUtils::transferModes, data -> {
                final Dragboard dragboard = getTreeTableView().startDragAndDrop(data.transferModes);
                final ClipboardContent content = new ClipboardContent();
                content.putString(item);
                dragboard.setContent(content);
            }));
            setOnDragOver(event -> copies.progress(currentItem(event), event.getTransferMode(), BeanTreeUtils::transferModes, (s, t) -> {
                event.acceptTransferModes(t.transferModes);
            }));
            setOnDragDropped(event -> copies.finish(currentItem(event), event.getTransferMode(), BeanTreeUtils::finishCopy));
            if (treeItem.getValue() instanceof Property) {
                setStyle("-fx-underline: true");
            } else {
                setStyle(null);
            }
        } else {
            setOnDragDetected(null);
            setOnDragOver(null);
            setOnDragDropped(null);
            setStyle(null);
        }
    }

    private TreeItem<Object> currentItem(Event event) {
        return ((NameCell) event.getSource()).getTreeTableRow().getTreeItem();
    }

    @Override
    public void startEdit() {
        final TreeTableRow<Object> row = getTreeTableRow();
        final TreeItem<Object> item = row.getTreeItem();
        if (item.getValue() instanceof BeanData || item.getValue() instanceof Path) {
            super.startEdit();
        }
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
