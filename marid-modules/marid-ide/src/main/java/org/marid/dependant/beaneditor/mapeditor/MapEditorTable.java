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

package org.marid.dependant.beaneditor.mapeditor;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.dependant.beaneditor.beandata.BeanPropEditor;
import org.marid.jfx.controls.CommonTableView;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.DElement;
import org.marid.spring.xml.DMap;
import org.marid.spring.xml.DMapEntry;
import org.marid.spring.xml.DValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class MapEditorTable extends CommonTableView<DMapEntry> {

    @Autowired
    public MapEditorTable(DMap map) {
        super(map.entries);
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<DMapEntry, String> column = new TableColumn<>();
        column.setCellValueFactory(param -> param.getValue().key);
        column.setEditable(true);
        column.setMinWidth(100);
        column.setMaxWidth(500);
        column.setPrefWidth(300);
        column.setCellFactory(c -> new TextFieldTableCell<>(new DefaultStringConverter()));
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void valueColumn() {
        final TableColumn<DMapEntry, DElement<?>> column = new TableColumn<>();
        column.setCellValueFactory(param -> param.getValue().value);
        column.setEditable(false);
        column.setMinWidth(300);
        column.setMaxWidth(2000);
        column.setPrefWidth(400);
        column.setCellFactory(c -> new TableCell<DMapEntry, DElement<?>>() {
            @Override
            protected void updateItem(DElement<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(BeanPropEditor.label(item));
                }
            }
        });
        getColumns().add(column);
    }

    @Autowired
    private void onAdd(FxAction addAction, DMap map) {
        addAction.on(this, a -> {
            a.setEventHandler(event -> {
                final DMapEntry entry = new DMapEntry();
                entry.key.set("key");
                entry.value.set(new DValue("#{null}"));
                map.entries.add(entry);
            });
        });
    }

    @Autowired
    private void initRowFactory(ResolvableType valueType, AutowireCapableBeanFactory factory) {
        setRowFactory(v -> {
            final TableRow<DMapEntry> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(new MaridContextMenu(m -> {
                m.getItems().clear();
                final DMapEntry entry = row.getItem();
                if (entry == null) {
                    return;
                }
                final ValueMenuItems menuItems = new ValueMenuItems(entry.value, valueType, emptyList(), entry.key);
                factory.initializeBean(menuItems, null);
                menuItems.addTo(m);
            }));
            return row;
        });
    }
}
