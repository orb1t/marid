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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.input.KeyCombination;
import org.marid.IdeDependants;
import org.marid.dependant.project.ProjectParams;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.dependant.project.runner.ProjectRunnerConfiguration;
import org.marid.ide.service.ProjectBuilderService;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectConfiguration {

    @Bean
    public BooleanBinding projectDisabled(ProjectManager manager) {
        return Bindings.selectBoolean(manager.profileProperty(), "enabled").not();
    }

    @IdeAction
    @Qualifier("profile")
    public FxAction projectSetup(IdeDependants dependants, ProjectManager manager, BooleanBinding projectDisabled) {
        return new FxAction("projectSetup", "setup", "Project")
                .bindText(ls("Project setup..."))
                .setIcon("O_TOOLS")
                .setEventHandler(event -> {
                    final ProjectProfile profile = manager.getProfile();
                    dependants.start(ProjectConfigConfiguration.class, new ProjectParams(profile), context -> {
                        context.setId("projectConfiguration");
                        context.setDisplayName("Project Configuration");
                    });
                })
                .bindDisabled(projectDisabled);
    }

    @IdeAction
    @Qualifier("profile")
    public FxAction projectSaveAction(ObjectFactory<ProjectSaver> projectSaver,
                                      ProjectManager manager,
                                      BooleanBinding projectDisabled) {
        return new FxAction("io", "Project")
                .setAccelerator(KeyCombination.valueOf("F2"))
                .bindText(ls("Save"))
                .setIcon("F_SAVE")
                .setEventHandler(event -> projectSaver.getObject().save(manager.getProfile()))
                .bindDisabled(projectDisabled);
    }

    @IdeAction
    @Qualifier("profile")
    public FxAction projectBuildAction(ObjectFactory<ProjectBuilderService> mavenBuilder,
                                       ObjectFactory<ProjectSaver> projectSaver,
                                       ProjectManager projectManager,
                                       BooleanBinding projectDisabled) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F9"))
                .bindText(ls("Build"))
                .setIcon("D_CLOCK_FAST")
                .setEventHandler(event -> {
                    final ProjectProfile profile = projectManager.getProfile();
                    projectSaver.getObject().save(profile);
                    mavenBuilder.getObject()
                            .setProfile(profile)
                            .start();
                })
                .bindDisabled(projectDisabled);
    }

    @IdeAction
    @Qualifier("profile")
    public FxAction projectBuildAllAction(ObjectFactory<ProjectBuilderService> mavenBuilder,
                                          ObjectFactory<ProjectSaver> projectSaver,
                                          ProjectManager projectManager,
                                          BooleanBinding projectDisabled) {
        return new FxAction("pb", "Project")
                .setAccelerator(KeyCombination.valueOf("Shift+F9"))
                .bindText(ls("Build All"))
                .setIcon("F_CLOCK_ALT")
                .setEventHandler(event -> {
                    for (final ProjectProfile profile : projectManager.getProfiles()) {
                        projectSaver.getObject().save(profile);
                        projectManager.profileProperty().set(profile);
                        mavenBuilder.getObject()
                                .setProfile(profile)
                                .start();
                    }
                })
                .bindDisabled(projectDisabled);
    }

    @IdeAction
    @Qualifier("profile")
    public FxAction projectRunAction(IdeDependants dependants, ProjectManager manager, BooleanBinding projectDisabled) {
        return new FxAction("projectBuild", "pb", "Project")
                .setAccelerator(KeyCombination.valueOf("F5"))
                .bindText(ls("Run"))
                .setIcon("F_PLAY")
                .setEventHandler(event -> {
                    final ProjectProfile profile = manager.getProfile();
                    dependants.start(ProjectRunnerConfiguration.class, new ProjectParams(profile), context -> {
                        context.setId("projectRunner");
                        context.setDisplayName("Project Runner");
                    });
                })
                .bindDisabled(projectDisabled);
    }
}
