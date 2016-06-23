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

import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.IdeDependants;
import org.marid.dependant.beandata.BeanDataEditorConfiguration;
import org.marid.ide.panes.tabs.IdeTabPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.l10n.L10nSupport;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanEditorTableConfiguration implements L10nSupport {

    @Bean
    public ToolBar beanEditorToolbar(AnnotationConfigApplicationContext context, BeanEditorTable table) {
        return new ToolbarBuilder()
                .add(s("Edit..."), FontIcon.M_EDIT,
                        event -> IdeDependants.startDependant(context, "beanConfigurer", BeanDataEditorConfiguration.class))
                .addSeparator()
                .add(s("Remove"), FontIcon.O_REPO_DELETE,
                        event -> table.getItems().remove(table.getSelectionModel().getSelectedIndex()))
                .add(s("Clear"), FontIcon.M_CLEAR_ALL,
                        event -> table.getItems().clear())
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
    public Tab tab(ProjectProfile profile, IdeTabPane tabPane, BorderPane beanEditor, Path beanFilePath) {
        final Path relativePath = profile.getBeansDirectory().relativize(beanFilePath);
        final Tab tab = new Tab(s("[%s]: %s", profile, relativePath), beanEditor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }
}
