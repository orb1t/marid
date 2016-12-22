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

package org.marid.dependant.beaneditor.beandata;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.marid.dependant.beaneditor.valuemenu.ValueMenuItems;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DCollection;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DValue;
import org.marid.spring.xml.ref.DRef;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

import java.util.function.Function;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BeanPropEditor extends TableView<BeanProp> {

    private final Function<String, ResolvableType> typeFunc;

    public BeanPropEditor(ObservableList<BeanProp> items, Function<String, ResolvableType> typeFunc) {
        super(items);
        this.typeFunc = typeFunc;
        setEditable(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<BeanProp, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Name"));
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<BeanProp, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setPrefWidth(250);
        col.setMaxWidth(520);
        col.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void valueColumn() {
        final TableColumn<BeanProp, Label> col = new TableColumn<>();
        col.textProperty().bind(ls("Value"));
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(param -> createObjectBinding(() -> {
            final Label label = new Label();
            final DElement<?> element = param.getValue().getData();
            if (element instanceof DRef) {
                label.setGraphic(glyphIcon(M_LINK, 16));
            } else if (element instanceof DValue) {
                label.setGraphic(glyphIcon(M_TEXT_FORMAT, 16));
            } else if (element instanceof DCollection) {
                label.setGraphic(glyphIcon(M_LIST, 16));
            }
            if (element != null) {
                label.setText(element.toString());
            }
            return label;
        }, param.getValue()));
        getColumns().add(col);
    }

    @Autowired
    public void initContextMenu(ObjectProvider<ValueMenuItems> items) {
        setRowFactory(param -> {
            final TableRow<BeanProp> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(new MaridContextMenu(m -> {
                if (row.getItem() != null) {
                    final ResolvableType type = typeFunc.apply(row.getItem().getName());
                    m.getItems().clear();
                    items.getObject(row.getItem().data, type).addTo(m.getItems());
                }
            }));
            return row;
        });
    }
}
