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

package org.marid.dependant.resources;

import javafx.scene.control.TabPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.ide.common.IdeShapes.profileNode;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ResourcesTab extends IdeTab {

    public ResourcesTab(ProjectProfile profile, TabPane resourcesTabPane) {
        super(resourcesTabPane, createStringBinding(profile::getName), () -> profileNode(profile, 16));
    }
}
