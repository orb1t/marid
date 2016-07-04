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

package org.marid.ide.project;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCombination;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.dependant.project.runner.ProjectRunnerConfiguration;
import org.marid.ide.project.cache.ProjectCacheManager;
import org.marid.jfx.action.FxAction;
import org.marid.l10n.L10n;
import org.marid.logging.LogSupport;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;
import static org.marid.IdeDependants.startDependant;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.misc.Calls.callWithTime;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ProjectConfiguration implements LogSupport {

    private final ProjectManager projectManager;

    @Autowired
    public ProjectConfiguration(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Bean
    @IdeAction
    public FxAction projectSetupAction() {
        return new FxAction("projectSetup", "setup", "Project")
                .setText("Project setup...")
                .setIcon(O_TOOLS)
                .setEventHandler(event -> startDependant("projectSetup", ProjectConfigConfiguration.class));
    }

    @Bean
    @IdeAction
    public FxAction projectSaveAction(ObjectFactory<ProjectSaver> projectSaver) {
        return new FxAction("projectIO", "io", "Project")
                .setAccelerator(KeyCombination.valueOf("Ctrl+S"))
                .setText("Save")
                .setIcon(F_SAVE)
                .setEventHandler(event -> callWithTime(() -> projectSaver.getObject().save(),
                        time -> log(INFO, "Profile [{0}] saved in {1} ms", projectManager.getProfile(), time)));
    }

    @Bean
    @IdeAction
    public FxAction projectBuildAction(ProjectCacheManager projectCacheManager) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F9"))
                .setText("Build")
                .setIcon(D_CLOCK_FAST)
                .setEventHandler(event -> projectCacheManager.build(projectManager.getProfile()));
    }

    @Bean
    @IdeAction
    public FxAction projectRunAction() {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F5"))
                .setText("Run")
                .setIcon(F_PLAY)
                .setEventHandler(event -> startDependant("projectSetup", ProjectRunnerConfiguration.class));
    }

    @Bean
    @IdeAction
    public FxAction projectAddProfileAction(ObjectFactory<ProjectSaver> projectSaverFactory) {
        return new FxAction("projectIO", "pm", "Project")
                .setText("Add profile...")
                .setIcon(M_ADD_BOX)
                .setEventHandler(event -> {
                    final TextInputDialog dialog = new TextInputDialog("profile");
                    dialog.setHeaderText(L10n.s("Profile name") + ":");
                    dialog.setTitle(L10n.s("Add profile"));
                    final Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        final ProjectProfile profile = projectManager.add(result.get());
                        try (final InputStream is = getClass().getResourceAsStream("/logging/default.properties")) {
                            Files.copy(is, profile.getSrcMainResources().resolve("logging.properties"), REPLACE_EXISTING);
                        } catch (Exception x) {
                            log(WARNING, "Unable to write default logging properties", x);
                        }
                        projectSaverFactory.getObject().save();
                    }
                });
    }

    @Bean
    @IdeAction
    public FxAction projectRemoveProfileAction() {
        return new FxAction("projectIO", "pm", "Project")
                .setText("Remove profile")
                .setIcon(D_MINUS_BOX)
                .setEventHandler(event -> {
                    final Alert alert = new Alert(CONFIRMATION, L10n.s("Do you really want to remove the profile?"), YES, NO);
                    alert.setTitle(L10n.s("Profile removal"));
                    alert.setHeaderText(L10n.s("Project removal confirmation"));
                    final Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        projectManager.remove(projectManager.getProfile());
                    }
                });
    }
}
