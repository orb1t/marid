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
import org.marid.ide.project.ProjectSaver;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProfilesTable extends TableView<ProjectProfile> implements LogSupport {

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
        final TableColumn<ProjectProfile, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Name"));
        column.setEditable(false);
        column.setPrefWidth(400);
        column.setMaxWidth(2000);
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void initHmiColumn() {
        final TableColumn<ProjectProfile, Boolean> column = new TableColumn<>();
        column.textProperty().bind(ls("HMI"));
        column.setMaxWidth(70);
        column.setCellFactory(param -> new CheckBoxTableCell<>());
        column.setCellValueFactory(param -> param.getValue().hmiProperty());
        getColumns().add(column);
    }

    @Autowired
    public void init(@Qualifier("profile") Map<String, FxAction> actionMap, SpecialActions specialActions) {
        setRowFactory(param -> {
            final TableRow<ProjectProfile> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(specialActions.contextMenu(() -> actionMap));
            return row;
        });
    }

    @Autowired
    private void initEdit(IdeDependants dependants, ProjectManager projectManager, FxAction editAction) {
        editAction.on(this, action -> {
            action.setEventHandler(event -> {
                final ProjectProfile profile = projectManager.getProfile();
                dependants.start(ResourcesConfiguration.class, profile.getName());
            });
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }

    @Autowired
    private void initAdd(FxAction addAction, ProjectSaver projectSaver, ProjectManager manager) {
        addAction.on(this, action -> action.setEventHandler(event -> {
            final TextInputDialog dialog = new TextInputDialog("profile");
            dialog.setHeaderText(s("Profile name") + ":");
            dialog.setTitle(s("Add profile"));
            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                final ProjectProfile profile = manager.add(result.get());
                try (final InputStream is = getClass().getResourceAsStream("/logging/default.properties")) {
                    Files.copy(is, profile.getSrcMainResources().resolve("logging.properties"), REPLACE_EXISTING);
                } catch (Exception x) {
                    log(WARNING, "Unable to write default logging properties", x);
                }
                manager.profileProperty().set(profile);
                projectSaver.save(profile);
            }
        }));
    }

    @Autowired
    private void initRemove(FxAction removeAction, ProjectManager manager) {
        removeAction.on(this, action -> {
            action.setEventHandler(event -> {
                final Alert alert = new Alert(CONFIRMATION, s("Do you really want to remove the profile?"), YES, NO);
                alert.setTitle(s("Profile removal"));
                alert.setHeaderText(s("Project removal confirmation"));
                final Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) {
                    manager.remove(manager.getProfile());
                }
            });
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }
}
