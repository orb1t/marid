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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.StringUtils;
import org.marid.ide.beaneditor.data.BeanData;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameCell extends TextFieldTreeTableCell<Object, String> {

    private final TreeTableColumn<Object, String> column;

    public NameCell(TreeTableColumn<Object, String> column) {
        this.column = column;
        setAlignment(Pos.CENTER_LEFT);
        setConverter(new DefaultStringConverter());
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
        } else {
            cancelEdit();
        }
    }
}
