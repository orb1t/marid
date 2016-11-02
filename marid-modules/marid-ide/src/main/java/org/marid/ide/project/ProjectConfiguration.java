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
import org.marid.IdeDependants;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.dependant.project.runner.ProjectRunnerConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javafx.beans.binding.Bindings.createBooleanBinding;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ProjectConfiguration implements LogSupport {

    @Bean
    public Supplier<ProjectProfile> profileSupplier(ProjectManager projectManager) {
        return projectManager::getProfile;
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectSetupAction(IdeDependants dependants, Supplier<ProjectProfile> profile) {
        return new FxAction("projectSetup", "setup", "Project")
                .setText(s("Project setup..."))
                .setIcon(O_TOOLS)
                .setEventHandler(event -> dependants.start(
                        "projectEditor",
                        ProjectConfigConfiguration.class,
                        c -> c.profile = profile.get()
                ));
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectSaveAction(ObjectFactory<ProjectSaver> projectSaver, Supplier<ProjectProfile> profile) {
        return new FxAction(null, "io", "Project")
                .setAccelerator(KeyCombination.valueOf("F2"))
                .setText(s("Save"))
                .setIcon(F_SAVE)
                .setEventHandler(event -> projectSaver.getObject().save(profile.get()));
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectBuildAction(ObjectFactory<ProjectMavenBuilder> mavenBuilder,
                                       ObjectFactory<ProjectSaver> projectSaver,
                                       Supplier<ProjectProfile> profileSupplier) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F9"))
                .setText(s("Build"))
                .setIcon(D_CLOCK_FAST)
                .setEventHandler(event -> {
                    final ProjectProfile profile = profileSupplier.get();
                    projectSaver.getObject().save(profile);
                    mavenBuilder.getObject().build(profile, result -> {
                        try {
                            log(INFO, "[{0}] Built {1}", profile, result);
                            profile.cacheEntry.update();
                            log(INFO, "[{0}] Updated", profile);
                        } catch (Exception x) {
                            log(WARNING, "Unable to update cache {0}", x, profile);
                        }
                    }, profile.logger()::log);
                })
                .setDisabled(false);
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectRunAction(IdeDependants dependants, Supplier<ProjectProfile> profile) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F5"))
                .setText(s("Run"))
                .setIcon(F_PLAY)
                .setEventHandler(event -> dependants.start(
                        "projectRunner",
                        ProjectRunnerConfiguration.class,
                        c -> c.profile = profile.get()
                ));
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectAddProfileAction(ObjectFactory<ProjectSaver> projectSaverFactory,
                                            ObjectFactory<ProjectManager> projectManager) {
        return new FxAction("projectIO", "pm", "Project")
                .setText(s("Add profile..."))
                .setIcon(M_ADD_BOX)
                .setEventHandler(event -> {
                    final TextInputDialog dialog = new TextInputDialog("profile");
                    dialog.setHeaderText(s("Profile name") + ":");
                    dialog.setTitle(s("Add profile"));
                    final Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        final ProjectProfile profile = projectManager.getObject().add(result.get());
                        try (final InputStream is = getClass().getResourceAsStream("/logging/default.properties")) {
                            Files.copy(is, profile.getSrcMainResources().resolve("logging.properties"), REPLACE_EXISTING);
                        } catch (Exception x) {
                            log(WARNING, "Unable to write default logging properties", x);
                        }
                        projectManager.getObject().profileProperty().set(profile);
                        projectSaverFactory.getObject().save(profile);
                    }
                });
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectRemoveProfileAction(ProjectManager manager, Supplier<ProjectProfile> profile) {
        return new FxAction("projectIO", "pm", "Project")
                .setText(s("Remove profile"))
                .setIcon(D_MINUS_BOX)
                .bindDisabled(createBooleanBinding(() -> manager.getProfiles().size() < 2, manager.getProfiles()))
                .setEventHandler(event -> {
                    final Alert alert = new Alert(CONFIRMATION, s("Do you really want to remove the profile?"), YES, NO);
                    alert.setTitle(s("Profile removal"));
                    alert.setHeaderText(s("Project removal confirmation"));
                    final Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        manager.remove(profile.get());
                    }
                });
    }
}
