/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.jfx.logging;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import org.marid.jfx.icons.IconFactory;

import java.util.logging.LogRecord;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogComponent extends ListView<LogRecord> {

    public LogComponent(ObservableList<LogRecord> records) {
        super(records);
        setCellFactory(p -> {
            final ListCell<LogRecord> cell = new ListCell<LogRecord>() {
                @Override
                protected void updateItem(LogRecord item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        setText(m(item.getMessage(), item.getParameters()));
                        setGraphic(IconFactory.icon(item.getLevel()));
                    }
                }
            };
            cell.setFont(Font.font("Monospaced", cell.getFont().getSize() * 0.75));
            return cell;
        });
        setContextMenu(new ContextMenu(
                removeItems(),
                new SeparatorMenuItem(),
                clearAllItem()
        ));
    }

    private MenuItem clearAllItem() {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty().bind(ls("Clear all"));
        menuItem.setOnAction(event -> getItems().clear());
        menuItem.disableProperty().bind(Bindings.isEmpty(getItems()));
        return menuItem;
    }

    private MenuItem removeItems() {
        final MenuItem menuItem = new MenuItem();
        menuItem.textProperty().bind(ls("Remove selected items"));
        menuItem.setOnAction(event -> getItems().retainAll(getSelectionModel().getSelectedItems()));
        menuItem.disableProperty().bind(Bindings.isEmpty(getSelectionModel().getSelectedItems()));
        return menuItem;
    }
}
