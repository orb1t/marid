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

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.input.KeyCode;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueCell extends TreeTableCell<Object, Label> {

    private final TreeTableColumn<Object, Label> column;

    public ValueCell(TreeTableColumn<Object, Label> column) {
        this.column = column;
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void updateItem(Label val, boolean empty) {
        super.updateItem(val, empty);
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(val);
        }
    }

    @Override
    public void startEdit() {
        final TreeTableRow<Object> row = getTreeTableRow();
        final TreeItem<Object> item = row.getTreeItem();
        final StringProperty textProperty;
        if (item.getValue() instanceof ConstructorArg) {
            textProperty = ((ConstructorArg) item.getValue()).value;
        } else if (item.getValue() instanceof Property) {
            textProperty = ((Property) item.getValue()).value;
        } else {
            return;
        }
        final TextField textField = new TextField(textProperty.get());
        textField.setOnAction(event -> {
            textProperty.set(textField.getText());
            final CellDataFeatures<Object, Label> f = new CellDataFeatures<>(getTreeTableView(), getTableColumn(), item);
            final Label label = getTableColumn().getCellValueFactory().call(f).getValue();
            commitEdit(label);
        });
        final Runnable cancel = () -> {
            cancelEdit();
            final CellDataFeatures<Object, Label> f = new CellDataFeatures<>(getTreeTableView(), getTableColumn(), item);
            final Label label = getTableColumn().getCellValueFactory().call(f).getValue();
            setGraphic(label);
        };
        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancel.run();
            }
        });
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                cancel.run();
            }
        });
        setGraphic(textField);
        textField.requestFocus();
    }
}
