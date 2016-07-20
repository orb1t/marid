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

package org.marid.dependant.beaneditor.beans.constants;

import javafx.event.ActionEvent;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.UtilConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ConstantListActions {

    private final ConstantListTable table;
    private final ProjectProfile profile;

    @Autowired
    public ConstantListActions(ConstantListTable table, ProjectProfile profile) {
        this.table = table;
        this.profile = profile;
    }

    public void onAdd(ActionEvent event) {
        final String newBeanName = ProjectCacheManager.generateBeanName(profile, "newConstant");
        final UtilConstant constant = new UtilConstant();
        constant.id.set(newBeanName);
        table.getItems().add(constant);
    }
}
