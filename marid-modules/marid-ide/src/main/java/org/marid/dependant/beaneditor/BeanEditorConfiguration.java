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

import javafx.collections.MapChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import org.marid.ide.panes.filebrowser.BeanFileBrowserTree;
import org.marid.ide.panes.tabs.IdeTabPane;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanEditorConfiguration implements L10nSupport {

    @Bean
    public ProjectProfile projectProfile(ProjectManager projectManager) {
        return projectManager.getProfile();
    }

    @Bean
    public Path beanFilePath(BeanFileBrowserTree tree) {
        return tree.getSelectionModel().getSelectedItem().getValue();
    }

    @Bean
    public BeanFile beanFile(Path beanFilePath, ProjectProfile profile) {
        return profile.getBeanFiles().get(beanFilePath);
    }

    @Bean
    public MapChangeListener<Path, BeanFile> beanFilesChangeListener(IdeTabPane ideTabPane,
                                                                     ProjectProfile profile,
                                                                     Tab tab,
                                                                     Path beanFilePath) {
        final MapChangeListener<Path, BeanFile> listener = change -> {
            final BeanFile beanFile = requireNonNull(profile.getBeanFiles().get(beanFilePath));
            if (change.wasRemoved()) {
                if (beanFile.equals(change.getKey())) {
                    ideTabPane.getTabs().remove(tab);
                }
            }
        };
        profile.getBeanFiles().addListener(listener);
        return listener;
    }

    @Bean
    public BorderPane beanEditor(BeanEditorTable table, AnnotationConfigApplicationContext context) {
        final BorderPane pane = new BorderPane(ScrollPanes.scrollPane(table));
        pane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                context.close();
            }
        });
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

    @Bean
    public ApplicationListener<ContextClosedEvent> contextClosedListener(ProjectProfile profile,
                                                                         MapChangeListener<Path, BeanFile> beanFileMapChangeListener) {
        return event -> profile.getBeanFiles().removeListener(beanFileMapChangeListener);
    }
}
