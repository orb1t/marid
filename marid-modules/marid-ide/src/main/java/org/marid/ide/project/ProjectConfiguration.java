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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.dependant.project.runner.ProjectRunnerConfiguration;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
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
public class ProjectConfiguration implements LogSupport, L10nSupport {

    private final ProjectManager projectManager;

    @Autowired
    public ProjectConfiguration(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Project setup...", group = "setup", icon = O_TOOLS)
    @IdeToolbarItem(group = "projectSetup")
    public EventHandler<ActionEvent> projectSetup() {
        return event -> startDependant("projectSetup", ProjectConfigConfiguration.class);
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Save", group = "io", icon = F_SAVE, key = "Ctrl+S")
    @IdeToolbarItem(group = "projectIO")
    public EventHandler<ActionEvent> projectSave(Provider<ProjectSaver> projectSaverProvider) {
        return event -> callWithTime(
                () -> projectSaverProvider.get().save(),
                time -> log(INFO, "Profile [{0}] saved in {1} ms", projectManager.getProfile(), time));
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Build", group = "pb", icon = D_CLOCK_FAST, key = "F9")
    @IdeToolbarItem(group = "projectBuild")
    public EventHandler<ActionEvent> projectBuild(Provider<ProjectManager> projectManager) {
        return event -> {
            final ProjectProfile profile = projectManager.get().getProfile();
            final MavenProjectBuilder mavenProjectBuilder = new MavenProjectBuilder(profile)
                    .goals("clean", "install");
            try {
                mavenProjectBuilder.build();
            } catch (Exception x) {
                log(WARNING, "Unable to build", x);
            }
        };
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Run", group = "pb", icon = F_PLAY, key = "F5")
    @IdeToolbarItem(group = "projectBuild")
    public EventHandler<ActionEvent> projectRun() {
        return event -> startDependant("projectSetup", ProjectRunnerConfiguration.class);
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Add profile...", group = "pm", icon = M_ADD_BOX)
    @IdeToolbarItem(group = "projectIO", id = "p_add")
    public EventHandler<ActionEvent> projectAddProfile(Provider<ProjectSaver> projectSaver) {
        return event -> {
            final TextInputDialog dialog = new TextInputDialog("profile");
            dialog.setHeaderText(s("Profile name") + ":");
            dialog.setTitle(s("Add profile"));
            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                final ProjectProfile profile = projectManager.add(result.get());
                try (final InputStream is = getClass().getResourceAsStream("/logging/default.properties")) {
                    Files.copy(is, profile.getSrcMainResources().resolve("logging.properties"), REPLACE_EXISTING);
                } catch (Exception x) {
                    log(WARNING, "Unable to write default logging properties", x);
                }
                projectSaver.get().save();
            }
        };
    }

    @Bean
    @IdeMenuItem(menu = "Project", text = "Remove profile", group = "pm", icon = D_MINUS_BOX)
    @IdeToolbarItem(group = "projectIO", id = "p_remove")
    public EventHandler<ActionEvent> projectRemoveProfile() {
        return event -> {
            final Alert alert = new Alert(CONFIRMATION, s("Do you really want to remove the profile?"), YES, NO);
            alert.setTitle(s("Profile removal"));
            alert.setHeaderText(s("Project removal confirmation"));
            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                projectManager.remove(projectManager.getProfile());
            }
        };
    }
}
