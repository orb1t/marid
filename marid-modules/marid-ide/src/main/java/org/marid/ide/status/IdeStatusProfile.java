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

package org.marid.ide.status;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import org.marid.ee.IdeSingleton;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class IdeStatusProfile extends ComboBox<ProjectProfile> {

    @Inject
    public IdeStatusProfile(ProjectManager projectManager) {
        super(FXCollections.observableArrayList(projectManager.getProfiles()));
        getSelectionModel().select(projectManager.getProfile());
        setFocusTraversable(false);
        setOnShowing(e -> {
            final Set<ProjectProfile> profiles = projectManager.getProfiles();
            getItems().retainAll(profiles);
            profiles.removeAll(getItems());
            getItems().addAll(profiles);
        });
        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            projectManager.profileProperty().set(newValue);
        });
    }
}
