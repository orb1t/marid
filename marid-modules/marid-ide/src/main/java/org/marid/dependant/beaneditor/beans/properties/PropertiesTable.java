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

package org.marid.dependant.beaneditor.beans.properties;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.dependant.beaneditor.beans.controls.NameColumn;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.data.UtilProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class PropertiesTable extends MaridTableView<UtilProperties> {

    @Autowired
    public PropertiesTable(BeanFile beanFile) {
        super(beanFile.properties);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
    }

    @OrderedInit(1)
    public void nameColumn(ProjectProfile profile) {
        final TableColumn<UtilProperties, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> param.getValue().nameProperty());
        col.setCellFactory(param -> new NameColumn<>(profile, c -> {
        }));
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void valueTypeColumn() {
        final TableColumn<UtilProperties, String> col = new TableColumn<>(s("Value type"));
        col.setCellValueFactory(param -> param.getValue().valueType);
        col.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void locationColumn() {
        final TableColumn<UtilProperties, String> col = new TableColumn<>(s("Location"));
        col.setCellValueFactory(param -> param.getValue().location);
        col.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col.setPrefWidth(400);
        col.setMaxWidth(1000);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(4)
    public void localOverrideColumn() {
        final TableColumn<UtilProperties, String> col = new TableColumn<>(s("Local override"));
        col.setCellValueFactory(param -> param.getValue().localOverride);
        col.setCellFactory(param -> new ComboBoxTableCell<>(new DefaultStringConverter(), "true", "false", ""));
        col.setPrefWidth(100);
        col.setMaxWidth(150);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(5)
    public void ignoreResourceNotFound() {
        final TableColumn<UtilProperties, String> col = new TableColumn<>(s("Ignore n/f"));
        col.setCellValueFactory(param -> param.getValue().ignoreResourceNotFound);
        col.setCellFactory(param -> new ComboBoxTableCell<>(new DefaultStringConverter(), "true", "false", ""));
        col.setPrefWidth(150);
        col.setMaxWidth(150);
        col.setEditable(true);
        getColumns().add(col);
    }
}
