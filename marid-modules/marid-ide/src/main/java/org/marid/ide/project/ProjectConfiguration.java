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

import javafx.scene.input.KeyCombination;
import org.marid.IdeDependants;
import org.marid.dependant.project.ProjectParams;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.dependant.project.runner.ProjectRunnerConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ProjectConfiguration implements LogSupport {

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectSetupAction(IdeDependants dependants, ProjectManager projectManager) {
        return new FxAction("projectSetup", "setup", "Project")
                .bindText(ls("Project setup..."))
                .setIcon(O_TOOLS)
                .setEventHandler(event -> {
                    final ProjectProfile profile = projectManager.getProfile();
                    dependants.start(ProjectConfigConfiguration.class, new ProjectParams(profile), context -> {
                        context.setId("projectConfiguration");
                        context.setDisplayName("Project Configuration");
                    });
                });
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectSaveAction(ObjectFactory<ProjectSaver> projectSaver, ProjectManager projectManager) {
        return new FxAction(null, "io", "Project")
                .setAccelerator(KeyCombination.valueOf("F2"))
                .bindText(ls("Save"))
                .setIcon(F_SAVE)
                .setEventHandler(event -> projectSaver.getObject().save(projectManager.getProfile()));
    }

    @Bean
    @IdeAction
    @Qualifier("profile")
    public FxAction projectBuildAction(ObjectFactory<ProjectMavenBuilder> mavenBuilder,
                                       ObjectFactory<ProjectSaver> projectSaver,
                                       ProjectManager projectManager) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F9"))
                .bindText(ls("Build"))
                .setIcon(D_CLOCK_FAST)
                .setEventHandler(event -> {
                    final ProjectProfile profile = projectManager.getProfile();
                    projectSaver.getObject().save(profile);
                    mavenBuilder.getObject().build(profile, result -> {
                        try {
                            log(INFO, "[{0}] Built {1}", profile, result);
                            profile.update();
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
    public FxAction projectRunAction(IdeDependants dependants, ProjectManager projectManager) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F5"))
                .bindText(ls("Run"))
                .setIcon(F_PLAY)
                .setEventHandler(event -> {
                    final ProjectProfile profile = projectManager.getProfile();
                    dependants.start(ProjectRunnerConfiguration.class, new ProjectParams(profile), context -> {
                        context.setId("projectRunner");
                        context.setDisplayName("Project Runner");
                    });
                });
    }
}
