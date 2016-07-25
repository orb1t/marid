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

package org.marid.dependant.beaneditor.constants;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
@Import({ConstantListActions.class, ConstantListTable.class})
public class ConstantListConfiguration {

    @Bean
    public ToolBar constantsToolbar(ConstantListActions actions, ConstantListTable table) {
        return new ToolbarBuilder()
                .add("Add", FontIcon.M_ADD, actions::onAdd)
                .addSeparator()
                .add("Remove", FontIcon.M_REMOVE, table::onDelete, table.changeDisabled)
                .addSeparator()
                .add("Clear", FontIcon.M_CLEAR_ALL, table::onClear, table.clearDisabled)
                .build();
    }

    @Bean
    public BorderPane constantsEditor(ConstantListTable table, ToolBar constantsToolbar) {
        return new BorderPane(new MaridScrollPane(table), constantsToolbar, null, null, null);
    }
}
