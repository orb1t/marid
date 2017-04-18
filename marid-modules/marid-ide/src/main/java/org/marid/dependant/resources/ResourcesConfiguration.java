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

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.marid.Ide;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcon;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.l10n.L10n.s;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan(basePackageClasses = {ResourcesConfiguration.class})
public class ResourcesConfiguration extends DependantConfiguration<ResourcesParams> {

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public SplitPane resourcesSplitPane(ResourcesTable resourcesTable, BeanFileBrowser fileBrowser) {
        final SplitPane splitPane = new SplitPane(fileBrowser, resourcesTable);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setFocusTraversable(true);
        return splitPane;
    }

    @Bean
    @Qualifier("resources")
    public FxAction importAction(ProjectProfile profile) {
        return new FxAction("res", "res", "Edit")
                .bindText("Import....")
                .setIcon(FontIcon.D_IMPORT)
                .setEventHandler(event -> {
                    final FileChooser chooser = new FileChooser();
                    chooser.setTitle(s("Import files"));
                    chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(s("All files"), "*.*"));
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
                });
    }
}
