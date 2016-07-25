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

package org.marid.dependant.beaneditor.beans;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
@Import({BeanBrowserTable.class, BeanListActions.class, BeanListTable.class, BeanMetaInfoProvider.class})
public class BeanListConfiguration {

    @Bean
    public ToolBar beanEditorToolbar(BeanListActions actions, BeanListTable table) {
        return new ToolbarBuilder()
                .add("Add", M_ADD, actions::onAddNew)
                .add("Edit...", M_EDIT, actions::onEdit, table.changeDisabled)
                .addSeparator()
                .add("Remove", O_REPO_DELETE, actions::onDelete, table.changeDisabled)
                .add("Clear", M_CLEAR_ALL, actions::onClear, table.clearDisabled)
                .addSeparator()
                .add("Browse", O_BROWSER, actions::onBrowse)
                .addSeparator()
                .add("Actions", M_CREDIT_CARD, actions::onShowPopup, table.changeDisabled)
                .build();
    }

    @Bean
    public BorderPane beanEditor(BeanListTable table, ToolBar beanEditorToolbar) {
        return new BorderPane(new MaridScrollPane(table), beanEditorToolbar, null, null, null);
    }
}
