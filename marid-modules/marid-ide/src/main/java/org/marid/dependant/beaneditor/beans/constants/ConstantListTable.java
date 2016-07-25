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

package org.marid.dependant.beaneditor.beans.constants;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.controls.NameColumn;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.UtilConstant;
import org.marid.spring.xml.providers.ConstantsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ConstantListTable extends MaridTableView<UtilConstant> {

    @Autowired
    public ConstantListTable(ConstantsProvider constantsProvider) {
        super(constantsProvider.constants());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
    }

    @OrderedInit(1)
    public void nameColumn(ProjectProfile profile) {
        final TableColumn<UtilConstant, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> param.getValue().nameProperty());
        col.setCellFactory(param -> new NameColumn<>(profile, c -> {
        }));
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void fieldColumn(ProjectProfile profile) {
        final TableColumn<UtilConstant, String> col = new TableColumn<>(s("Field"));
        col.setCellValueFactory(param -> param.getValue().staticField);
        col.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col.setPrefWidth(400);
        col.setMaxWidth(1000);
        col.setEditable(true);
        getColumns().add(col);
    }
}