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

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.common.ValueMenuItems;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.project.ProjectProfileReflection;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.AbstractData;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.data.RefValue;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RefValuesEditor<T extends RefValue<T>> extends TableView<T> {

    private final Function<String, Optional<? extends Type>> typeFunc;
    private final Map<T, InvalidationListener> invalidationListenerMap = new HashMap<>();

    public RefValuesEditor(ObservableList<T> items, Function<String, Optional<? extends Type>> typeFunc) {
        super(items);
        this.typeFunc = typeFunc;
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
    public void refColumn(ProjectProfile profile, ProjectProfileReflection reflection) {
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
                    final Optional<? extends Type> bco = typeFunc.apply(data.name.get());
                    if (bco.isPresent()) {
                        getItems().clear();
                        for (final Pair<Path, BeanFile> beanFile : profile.getBeanFiles()) {
                            beanFile.getValue().allBeans().forEach(b -> {
                                final Optional<? extends Type> co = reflection.getType(b);
                                if (co.isPresent()) {
                                    if (TypeUtils.isAssignable(co.get(), bco.get())) {
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
    public void valueColumn() {
        final TableColumn<T, String> col = new TableColumn<>(s("Value"));
        col.setEditable(true);
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(param -> param.getValue().value);
        col.setCellFactory(param -> new TextFieldTableCell<T, String>(new DefaultStringConverter()) {
            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                final T data = getItems().get(getTableRow().getIndex());
                data.ref.set(null);
            }
        });
        getColumns().add(col);
    }

    @Autowired
    public void initContextMenu(ProjectProfile profile, IdeDependants dependants, ProjectProfileReflection reflection) {
        setRowFactory(param -> {
            final TableRow<T> row = new TableRow<>();
            row.itemProperty().addListener((o, ov, nv) -> {
                if (nv == null) {
                    row.setContextMenu(null);
                    invalidationListenerMap.computeIfPresent(ov, (v, old) -> {
                        v.removeListener(old);
                        return null;
                    });
                } else {
                    final InvalidationListener listener = observable -> {
                        final ContextMenu menu = new ContextMenu();
                        final Type type = reflection.getType(nv).orElse(null);
                        final ValueMenuItems items = new ValueMenuItems(dependants, nv.data, type);
                        menu.getItems().addAll(items.menuItems());
                        row.setContextMenu(menu);
                    };
                    listener.invalidated(nv);
                    invalidationListenerMap.compute(nv, (v, old) -> {
                        if (old != null) {
                            v.removeListener(old);
                        }
                        return listener;
                    });
                    nv.addListener(listener);
                }
            });
            return row;
        });
    }

    @PreDestroy
    public void destroy() {
        invalidationListenerMap.forEach(AbstractData::removeListener);
    }
}
