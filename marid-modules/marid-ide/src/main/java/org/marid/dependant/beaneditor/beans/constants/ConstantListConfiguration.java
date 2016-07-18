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

import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.spring.xml.data.UtilConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class ConstantListConfiguration {

    @Bean
    public ToolBar constantsToolbar(ProjectProfile profile, ConstantListTable table) {
        return new ToolbarBuilder()
                .add("Add", FontIcon.M_ADD, event -> {
                    final String newBeanName = ProjectCacheManager.generateBeanName(profile, "newConstant");
                    final UtilConstant constant = new UtilConstant();
                    constant.id.set(newBeanName);
                    table.getItems().add(constant);
                })
                .build();
    }

    @Bean
    public BorderPane constantsEditor(ConstantListTable table, ToolBar constantsToolbar) {
        return new BorderPane(ScrollPanes.scrollPane(table), constantsToolbar, null, null, null);
    }
}
