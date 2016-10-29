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

package org.marid.dependant.resources;

import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.marid.Ide;
import org.marid.dependant.resources.beanfiles.BeanFileBrowser;
import org.marid.dependant.resources.beanfiles.BeanFileBrowserActions;
import org.marid.dependant.resources.beanfiles.BeanFileBrowserPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.logging.LogSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({
        ResourcesTable.class,
        ResourcesTracker.class,
        BeanFileBrowser.class,
        BeanFileBrowserActions.class,
        BeanFileBrowserPane.class,
        ResourcesTab.class
})
public class ResourcesConfiguration implements LogSupport {

    public ProjectProfile profile;

    @Bean
    public ProjectProfile profile() {
        return profile;
    }

    @Bean
    public TabPane resourcesTabPane(BorderPane resourcesPane, BeanFileBrowserPane beanFileBrowserPane) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Bean files"), beanFileBrowserPane),
                new Tab(s("Resource files"), resourcesPane)
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setSide(Side.BOTTOM);
        return tabPane;
    }

    @Bean
    public BorderPane resourcesPane(ToolBar resourcesToolBar, ResourcesTable resourcesTable) {
        return new BorderPane(
                new MaridScrollPane(resourcesTable),
                resourcesToolBar,
                null,
                null,
                null
        );
    }

    @Bean
    public ToolBar resourcesToolBar(ProjectProfile profile) {
        return new ToolbarBuilder()
                .add("Import files...", FontIcon.M_IMPORT_EXPORT, event -> {
                    final FileChooser chooser = new FileChooser();
                    chooser.setTitle(s("Import files"));
                    chooser.getExtensionFilters().addAll(new ExtensionFilter(s("All files"), "*.*"));
                    final List<File> files = chooser.showOpenMultipleDialog(Ide.primaryStage);
                    if (files != null) {
                        final DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setInitialDirectory(profile.getSrcMainResources().toFile());
                        directoryChooser.setTitle(s("Target directory"));
                        final File directory = directoryChooser.showDialog(Ide.primaryStage);
                        if (directory != null) {
                            for (final File source : files) {
                                final File target = new File(directory, source.getName());
                                try {
                                    Files.copy(source.toPath(), target.toPath(), REPLACE_EXISTING);
                                    log(INFO, "Copied from {0} to {1}", source, target);
                                } catch (Exception x) {
                                    log(WARNING, "Unable to copy {0} to {1}", x, source, target);
                                }
                            }
                        }
                    }
                })
                .build();
    }
}
