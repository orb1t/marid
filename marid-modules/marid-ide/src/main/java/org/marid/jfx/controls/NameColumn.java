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

package org.marid.jfx.controls;

import javafx.scene.control.cell.TextFieldTableCell;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanLike;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 */
public class NameColumn<T extends BeanLike> extends TextFieldTableCell<T, String> {

    private final ProjectProfile profile;
    private final Consumer<NameColumn<T>> updateTask;

    public NameColumn(ProjectProfile profile, Consumer<NameColumn<T>> updateTask) {
        this.profile = profile;
        this.updateTask = updateTask;
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setContextMenu(null);
        } else {
            updateTask.accept(this);
        }
    }

    @Override
    public void commitEdit(String newValue) {
        final String oldValue = getItem();
        newValue = ProjectProfile.generateBeanName(profile, newValue);
        super.commitEdit(newValue);
        ProjectManager.onBeanNameChange(profile, oldValue, newValue);
    }
}
