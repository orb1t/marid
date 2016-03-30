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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;

import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueCell extends TreeTableCell<Object, Node> {

    private final TreeTableColumn<Object, Node> column;

    public ValueCell(TreeTableColumn<Object, Node> column) {
        this.column = column;
        setAlignment(Pos.CENTER_LEFT);
        setContentDisplay(ContentDisplay.RIGHT);
        setGraphicTextGap(10);
    }

    @Override
    public void updateItem(Node val, boolean empty) {
        super.updateItem(val, empty);
        setGraphic(val);
    }

    @Override
    public void startEdit() {
        final TreeItem<Object> item = getTreeTableRow().getTreeItem();
        final TreeTableColumn<Object, Node> column = getTableColumn();
        final TextInputDialog dialog = new TextInputDialog("A");
        final Optional<String> value = dialog.showAndWait();
        final ValueFactory valueFactory = (ValueFactory) column.getCellValueFactory();
        if (value.isPresent()) {
            final String val = value.get();
            if (item.getValue() instanceof Property) {
                final Property d = (Property) item.getValue();
                d.value.set(val);
                d.ref.set(null);
            } else if (item.getValue() instanceof ConstructorArg) {
                final ConstructorArg d = (ConstructorArg) item.getValue();
                d.value.set(val);
                d.ref.unbind();
                d.ref.set(null);
            }
            commitEdit(valueFactory.call(new CellDataFeatures<>(getTreeTableView(), column, item)).getValue());
        } else {
            cancelEdit();
        }
    }
}
