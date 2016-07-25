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

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanFile;
import org.marid.spring.xml.data.RefValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @version 0.8
 */
@Component
public class RefValuesEditorProvider {

    private final ProjectProfile profile;

    @Autowired
    public RefValuesEditorProvider(ProjectProfile profile) {
        this.profile = profile;
    }

    public <T extends RefValue<T>> TableView<T> newEditor(ObservableList<T> values) {
        final TableView<T> table = new TableView<>(values);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(true);
        {
            final TableColumn<T, String> col = new TableColumn<>(s("Name"));
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().name);
            col.setEditable(false);
            table.getColumns().add(col);
        }
        {
            final TableColumn<T, String> col = new TableColumn<>(s("Type"));
            col.setEditable(false);
            col.setPrefWidth(250);
            col.setMaxWidth(520);
            col.setCellValueFactory(param -> param.getValue().type);
            table.getColumns().add(col);
        }
        {
            final TableColumn<T, String> col = new TableColumn<>(s("Reference"));
            col.setEditable(true);
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().ref);
            col.setCellFactory(param -> {
                final ComboBoxTableCell<T, String> cell = new ComboBoxTableCell<T, String>(new DefaultStringConverter()) {
                    @Override
                    public void startEdit() {
                        final T data = table.getItems().get(getTableRow().getIndex());
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
                        final T data = table.getItems().get(getTableRow().getIndex());
                        data.value.set(null);
                    }
                };
                cell.setComboBoxEditable(true);
                return cell;
            });
            table.getColumns().add(col);
        }
        {
            final TableColumn<T, String> col = new TableColumn<>(s("Value"));
            col.setEditable(true);
            col.setPrefWidth(500);
            col.setMaxWidth(1500);
            col.setCellValueFactory(param -> param.getValue().value);
            col.setCellFactory(param -> new TextFieldTableCell<T, String>(new DefaultStringConverter()) {
                @Override
                public void commitEdit(String newValue) {
                    super.commitEdit(newValue);
                    final T data = table.getItems().get(getTableRow().getIndex());
                    data.ref.set(null);
                }
            });
            table.getColumns().add(col);
        }
        return table;
    }
}
