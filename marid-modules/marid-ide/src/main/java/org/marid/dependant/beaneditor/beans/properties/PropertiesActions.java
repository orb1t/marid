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

package org.marid.dependant.beaneditor.beans.properties;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.spring.xml.data.UtilProperties;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class PropertiesActions {

    private final ProjectProfile profile;
    private final PropertiesTable table;
    private final ObjectFactory<PropertyListTable> propertyListTable;

    @Autowired
    public PropertiesActions(ProjectProfile profile,
                             PropertiesTable table,
                             ObjectFactory<PropertyListTable> propertyListTable) {
        this.profile = profile;
        this.table = table;
        this.propertyListTable = propertyListTable;
    }

    public void onAdd(ActionEvent event) {
        final String name = ProjectCacheManager.generateBeanName(profile, "newProperties");
        final UtilProperties properties = new UtilProperties();
        properties.id.set(name);
        table.getItems().add(properties);
    }

    public void onEdit(ActionEvent event) {
        final PropertyListTable propertyListTable = this.propertyListTable.getObject();
        final Stage stage = new Stage();
        stage.setTitle(s("Property list %s", propertyListTable.properties.id.get()));
        stage.setScene(new Scene(new BorderPane(
                new MaridScrollPane(propertyListTable),
                new ToolbarBuilder()
                        .build(),
                null, null, null
        ), 800, 600));
        stage.show();
    }
}
