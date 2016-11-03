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

package org.marid.ide.panes.profiles;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import org.marid.IdeDependants;
import org.marid.dependant.resources.ResourcesConfiguration;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridMenu;
import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProfilesTable extends TableView<ProjectProfile> {

    public ProfilesTable(ProjectManager manager) {
        super(manager.getProfiles());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        getSelectionModel().select(manager.getProfile());
        final ChangeListener<ProjectProfile> listener = ($, o, v) -> manager.profileProperty().setValue(v);
        getSelectionModel().selectedItemProperty().addListener(listener);
        manager.profileProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                getSelectionModel().selectedItemProperty().removeListener(listener);
                getSelectionModel().select(newValue);
                getSelectionModel().selectedItemProperty().addListener(listener);
            }
        });
    }

    @OrderedInit(1)
    public void initNameColumn() {
        final TableColumn<ProjectProfile, String> column = new TableColumn<>(s("Name"));
        column.setEditable(false);
        column.setPrefWidth(400);
        column.setMaxWidth(2000);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void initHmiColumn() {
        final TableColumn<ProjectProfile, Boolean> column = new TableColumn<>(s("HMI"));
        column.setPrefWidth(60);
        column.setMaxWidth(70);
        column.setCellFactory(param -> new CheckBoxTableCell<>());
        column.setCellValueFactory(param -> param.getValue().hmiProperty());
        getColumns().add(column);
    }

    @Autowired
    public void init(@Qualifier("profile") Map<String, FxAction> actionMap) {
        setRowFactory(param -> {
            final TableRow<ProjectProfile> row = new TableRow<>();
            row.setContextMenu(new ContextMenu(new MaridMenu(actionMap).getMenus().stream()
                    .flatMap(m -> m.getItems().stream())
                    .toArray(MenuItem[]::new)));
            return row;
        });
    }

    @Autowired
    private void init(IdeDependants dependants, Supplier<ProjectProfile> profile, SpecialActions specialActions) {
        specialActions.setEditAction(this, ls("Project resources"), event -> {
            final String name = profile.get().getName();
            dependants.start(name, ResourcesConfiguration.class, c -> c.profile = profile.get());
        });
    }
}
