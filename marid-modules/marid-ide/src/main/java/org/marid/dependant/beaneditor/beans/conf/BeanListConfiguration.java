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

package org.marid.dependant.beaneditor.beans.conf;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.dependant.beaneditor.beans.BeanBrowserTable;
import org.marid.dependant.beaneditor.beans.BeanEditorActions;
import org.marid.dependant.beaneditor.beans.BeanEditorTable;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.spring.annotation.PrototypeBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.marid.Ide.primaryStage;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class BeanListConfiguration {

    @Bean
    public ToolBar beanEditorToolbar(BeanEditorActions actions) {
        return new ToolbarBuilder()
                .add("Add", M_ADD, actions::onAddNew)
                .add("Edit...", M_EDIT, actions::onEdit, actions.itemActionDisabled)
                .addSeparator()
                .add("Remove", O_REPO_DELETE, actions::onDelete, actions.itemActionDisabled)
                .add("Clear", M_CLEAR_ALL, actions::onClear, actions.clearDisabled)
                .addSeparator()
                .add("Browse", O_BROWSER, actions::onBrowse)
                .addSeparator()
                .add("Actions", M_CREDIT_CARD, actions::onShowPopup, actions.itemActionDisabled)
                .build();
    }

    @Bean
    public BorderPane beanEditor(BeanEditorTable table, ToolBar beanEditorToolbar) {
        final BorderPane pane = new BorderPane();
        pane.setCenter(ScrollPanes.scrollPane(table));
        pane.setTop(beanEditorToolbar);
        return pane;
    }

    @PrototypeBean
    public Dialog<List<Map.Entry<String, BeanDefinition>>> beanBrowser(BeanBrowserTable beans) {
        return new MaridDialog<List<Map.Entry<String, BeanDefinition>>>(primaryStage, new ButtonType(s("Add"), OK_DONE), CANCEL)
                .preferredSize(1024, 768)
                .title("Bean browser")
                .with((d, p) -> d.setResizable(true))
                .result(beans.getSelectionModel()::getSelectedItems)
                .with((d, p) -> p.setContent(ScrollPanes.scrollPane(beans)));
    }
}
