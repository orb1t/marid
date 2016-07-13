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

package org.marid.dependant.beaneditor;

import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.l10n.L10n.s;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanEditorTableConfiguration {

    @Bean
    public ToolBar beanEditorToolbar(BeanEditorActions actions) {
        return new ToolbarBuilder()
                .add(s("Edit..."), M_EDIT, actions::onEdit, actions.itemActionDisabled)
                .addSeparator()
                .add(s("Remove"), O_REPO_DELETE, actions::onDelete, actions.itemActionDisabled)
                .add(s("Clear"), M_CLEAR_ALL, actions::onClear, actions.clearDisabled)
                .addSeparator()
                .add(s("Browse"), O_BROWSER, actions::onBrowse)
                .addSeparator()
                .add(s("Actions"), M_CREDIT_CARD, actions::onShowPopup, actions.itemActionDisabled)
                .build();
    }

    @Bean
    public BorderPane beanEditor(BeanEditorTable table, ToolBar beanEditorToolbar, AnnotationConfigApplicationContext context) {
        final BorderPane pane = new BorderPane();
        pane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                context.close();
            }
        });
        pane.setCenter(ScrollPanes.scrollPane(table));
        pane.setTop(beanEditorToolbar);
        return pane;
    }

    @Bean
    public Tab tab(ProjectProfile profile, TabPane tabPane, BorderPane beanEditor, Path beanFilePath) {
        final Path relativePath = profile.getBeansDirectory().relativize(beanFilePath);
        final Tab tab = new Tab(s("[%s]: %s", profile, relativePath), beanEditor);
        tab.getProperties().put("profile", profile);
        tab.getProperties().put("path", beanFilePath);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Dialog<List<Entry<String, BeanDefinition>>> beanBrowser(IdePane idePane, BeanBrowserTable beans) {
        final Dialog<List<Entry<String, BeanDefinition>>> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(idePane.getScene().getWindow());
        dialog.getDialogPane().setPrefSize(1024, 768);
        dialog.setTitle(s("Bean browser"));
        dialog.setResizable(true);
        dialog.setResultConverter(p -> p.getButtonData() == OK_DONE ? beans.getSelectionModel().getSelectedItems() : null);
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType(s("Add"), OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(ScrollPanes.scrollPane(beans));
        return dialog;
    }
}
