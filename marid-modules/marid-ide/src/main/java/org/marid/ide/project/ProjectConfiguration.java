/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.project;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.input.KeyCombination;
import org.marid.dependant.project.config.ProjectConfigConfiguration;
import org.marid.ide.IdeDependants;
import org.marid.ide.service.ProjectBuilderService;
import org.marid.ide.service.ProjectRunService;
import org.marid.jfx.action.FxAction;
import org.marid.idelib.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLConnection;

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
          final ProjectProfile p = manager.getProfile();
          dependants.start(new ProjectConfigConfiguration(p), "Project Configuration");
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
                                     ProjectSaver projectSaver,
                                     ProjectManager projectManager,
                                     BooleanBinding projectDisabled) {
    return new FxAction("projectBuild", "pb", "Project")
        .setAccelerator(KeyCombination.valueOf("F9"))
        .bindText(ls("Build"))
        .setIcon("D_CLOCK_FAST")
        .setEventHandler(event -> {
          final ProjectProfile profile = projectManager.getProfile();
          projectSaver.save(profile);
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
  public FxAction projectRunAction(ProjectManager manager,
                                   ObjectFactory<ProjectRunService> projectRunService,
                                   BooleanBinding projectDisabled) {
    return new FxAction("projectBuild", "pb", "Project")
        .setAccelerator(KeyCombination.valueOf("F5"))
        .bindText(ls("Run"))
        .setIcon("F_PLAY")
        .setEventHandler(event -> projectRunService.getObject()
            .setProfile(manager.getProfile())
            .start())
        .bindDisabled(projectDisabled);
  }

  @PostConstruct
  private void disableUrlCache() throws Exception {
    final URLConnection connection = new URLConnection(null) {
      @Override
      public void connect() throws IOException {

      }
    };
    connection.setDefaultUseCaches(false);
  }
}
