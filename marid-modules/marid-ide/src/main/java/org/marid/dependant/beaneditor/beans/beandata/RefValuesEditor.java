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

package org.marid.dependant.beaneditor.beans.beandata;

import com.google.common.collect.ImmutableMap;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.beans.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.beans.propeditor.PropEditorConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.data.RefValue;
import org.marid.spring.xml.data.ValueHolder;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;

import java.lang.reflect.Type;
import java.util.*;

import static org.marid.jfx.icons.FontIcon.M_CLEAR;
import static org.marid.jfx.icons.FontIcon.M_MODE_EDIT;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RefValuesEditor<T extends RefValue<T>> extends TableView<T> {

    public RefValuesEditor(ObservableList<T> items) {
        super(items);
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<T, String> col = new TableColumn<>(s("Name"));
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> param.getValue().name);
        col.setEditable(false);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<T, String> col = new TableColumn<>(s("Type"));
        col.setEditable(false);
        col.setPrefWidth(250);
        col.setMaxWidth(520);
        col.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void refColumn(ProjectProfile profile) {
        final TableColumn<T, String> col = new TableColumn<>(s("Reference"));
        col.setEditable(true);
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> param.getValue().ref);
        col.setCellFactory(param -> {
            final ComboBoxTableCell<T, String> cell = new ComboBoxTableCell<T, String>(new DefaultStringConverter()) {
                @Override
                public void startEdit() {
                    final T data = RefValuesEditor.this.getItems().get(getTableRow().getIndex());
                    final Optional<Class<?>> bco = profile.getClass(data.type.get());
                    if (bco.isPresent()) {
                        getItems().clear();
                        for (final BeanFile beanFile : profile.getBeanFiles().values()) {
                            beanFile.allBeans().forEach(b -> {
                                final Optional<Class<?>> co = b.getClass(profile);
                                if (co.isPresent()) {
                                    if (bco.get().isAssignableFrom(co.get())) {
                                        getItems().add(b.nameProperty().get());
                                    }
                                }
                            });
                        }
                    }
                    super.startEdit();
                }

                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    final T data = RefValuesEditor.this.getItems().get(getTableRow().getIndex());
                    data.value.set(null);
                }
            };
            cell.setComboBoxEditable(true);
            return cell;
        });
        getColumns().add(col);
    }

    @OrderedInit(4)
    public void valueColumn(ProjectProfile profile, IdeDependants dependants) {
        final TableColumn<T, String> col = new TableColumn<>(s("Value"));
        col.setEditable(true);
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(param -> param.getValue().value);
        col.setCellFactory(param -> new TextFieldTableCell<T, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setContextMenu(null);
                } else {
                    final ContextMenu menu = new ContextMenu();
                    final Runnable persistentItemsAdder = () -> {
                        {
                            final MenuItem mi = new MenuItem(s("Clear value"), glyphIcon(M_CLEAR, 16));
                            mi.setOnAction(event -> {
                                final T data = getItems().get(getTableRow().getIndex());
                                data.value.set(null);
                            });
                            menu.getItems().add(mi);
                        }
                    };
                    persistentItemsAdder.run();
                    setContextMenu(menu);
                    menu.setOnShowing(event -> {
                        final T data = getItems().get(getTableRow().getIndex());
                        menu.getItems().clear();
                        persistentItemsAdder.run();
                        final List<MenuItem> valueItems = valueItems(profile, dependants, data);
                        if (!valueItems.isEmpty()) {
                            menu.getItems().add(new SeparatorMenuItem());
                        }
                        menu.getItems().addAll(valueItems);
                    });
                }
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                final T data = getItems().get(getTableRow().getIndex());
                data.ref.set(null);
            }
        });
        getColumns().add(col);
    }

    public static List<MenuItem> valueItems(ProjectProfile profile, IdeDependants dependants, ValueHolder<?> holder) {
        final Type type = holder.getType(profile).orElse(null);
        if (type == null) {
            return Collections.emptyList();
        }
        final List<MenuItem> items = new ArrayList<>();
        if (TypeUtils.isAssignable(type, Properties.class)) {
            final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
            final DProps props;
            if (holder.props.isNull().get()) {
                props = new DProps();
                holder.props.set(props);
            } else {
                props = holder.props.get();
                final MenuItem clearItem = new MenuItem(s("Clear properties"), glyphIcon(M_CLEAR, 16));
                clearItem.setOnAction(ev -> holder.props.set(null));
                items.add(clearItem);
            }
            mi.setOnAction(e -> dependants.start(PropEditorConfiguration.class, ImmutableMap.of("props", props)));
            items.add(mi);
        } else if (TypeUtils.isAssignable(type, List.class)) {
            if (!items.isEmpty()) {
                items.add(new SeparatorMenuItem());
            }
            final MenuItem mi = new MenuItem(s("Edit list..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                final DList list;
                if (holder.list.isNull().get()) {
                    list = new DList();
                    holder.list.set(list);
                } else {
                    list = holder.list.get();
                    final MenuItem clearItem = new MenuItem(s("Clear list"), glyphIcon(M_CLEAR, 16));
                    clearItem.setOnAction(ev -> holder.list.set(null));
                    items.add(clearItem);
                }
                dependants.start(ListEditorConfiguration.class, ImmutableMap.of("list", list));
            });
            items.add(mi);
        }
        return items;
    }
}
