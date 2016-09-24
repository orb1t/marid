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

package org.marid.dependant.beaneditor.propeditor;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.props.DPropEntry;
import org.marid.spring.xml.data.props.DProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class PropTable extends MaridTableView<DPropEntry> {

    @Autowired
    public PropTable(DProps props) {
        super(props.entries);
    }

    @OrderedInit(1)
    public void keyColumn() {
        final TableColumn<DPropEntry, String> column = new TableColumn<>(s("Key"));
        column.setCellValueFactory(param -> param.getValue().key);
        column.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        column.setPrefWidth(150);
        column.setMaxWidth(550);
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void valueColumn() {
        final TableColumn<DPropEntry, String> column = new TableColumn<>(s("Value"));
        column.setCellValueFactory(param -> param.getValue().value);
        column.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        column.setPrefWidth(350);
        column.setMaxWidth(2000);
        getColumns().add(column);
    }
}
