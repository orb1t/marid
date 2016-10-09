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
import javafx.scene.control.*;
import org.marid.dependant.beaneditor.valuemenu.ValueMenuItems;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.RefValue;
import org.marid.spring.xml.collection.DCollection;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DValue;
import org.marid.spring.xml.ref.DRef;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RefValuesEditor<T extends RefValue<T>> extends TableView<T> {

    private final Function<String, Optional<? extends Type>> typeFunc;

    public RefValuesEditor(ObservableList<T> items, Function<String, Optional<? extends Type>> typeFunc) {
        super(items);
        this.typeFunc = typeFunc;
        setEditable(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<T, String> col = new TableColumn<>(s("Name"));
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<T, String> col = new TableColumn<>(s("Type"));
        col.setPrefWidth(250);
        col.setMaxWidth(520);
        col.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void valueColumn() {
        final TableColumn<T, Label> col = new TableColumn<>(s("Value"));
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(param -> createObjectBinding(() -> {
            final Label label = new Label();
            final DElement<?> element = param.getValue().getData();
            if (element instanceof DRef) {
                label.setGraphic(FontIcons.glyphIcon(FontIcon.M_LINK, 16));
            } else if (element instanceof DValue) {
                label.setGraphic(FontIcons.glyphIcon(FontIcon.M_TEXT_FORMAT, 16));
            } else if (element instanceof DCollection) {
                label.setGraphic(FontIcons.glyphIcon(FontIcon.M_LIST, 16));
            }
            if (element != null) {
                label.setText(element.toString());
            }
            return label;
        }));
        getColumns().add(col);
    }

    @Autowired
    public void initContextMenu(ObjectProvider<ValueMenuItems> items) {
        setRowFactory(param -> new TableRow<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setContextMenu(null);
                } else {
                    final Type type = typeFunc.apply(item.getName()).orElse(null);
                    final Type typeArg = type == null ? Object.class : type;
                    final ContextMenu menu = new ContextMenu();
                    items.getObject(item.data, typeArg).addTo(menu);
                    setContextMenu(menu);
                }
            }
        });
    }
}
