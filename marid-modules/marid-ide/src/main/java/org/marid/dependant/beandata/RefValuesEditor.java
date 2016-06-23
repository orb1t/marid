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

package org.marid.dependant.beandata;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.project.event.BeanNameChangedEvent;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.RefValue;
import org.springframework.context.ApplicationEventPublisher;

import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class RefValuesEditor<T extends RefValue<T>> extends TableView<T> implements L10nSupport {

    private final ProjectProfile profile;

    public RefValuesEditor(ProjectProfile profile, ApplicationEventPublisher eventPublisher, ObservableList<T> list) {
        super(list);
        this.profile = profile;
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(s("Name"));
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().name);
            col.setEditable(true);
            col.setCellFactory(param -> new TextFieldTableCell<T, String>() {
                @Override
                public void commitEdit(String newValue) {
                    final String oldValue = getSelectionModel().getSelectedItem().name.get();
                    super.commitEdit(newValue);
                    eventPublisher.publishEvent(new BeanNameChangedEvent(profile, oldValue, newValue));
                }
            });
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(s("Type"));
            col.setEditable(false);
            col.setPrefWidth(250);
            col.setMaxWidth(520);
            col.setCellValueFactory(param -> param.getValue().type);
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(s("Reference"));
            col.setEditable(false);
            col.setPrefWidth(200);
            col.setMaxWidth(400);
            col.setCellValueFactory(param -> param.getValue().ref);
        }));
        getColumns().add(build(new TableColumn<T, String>(), col -> {
            col.setText(s("Value"));
            col.setEditable(false);
            col.setPrefWidth(500);
            col.setMaxWidth(1500);
            col.setCellValueFactory(param -> param.getValue().value);
        }));
    }
}
