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

import org.marid.ide.common.Directories;
import org.marid.ide.settings.JavaSettings;
import org.marid.io.ProcessManager;
import org.marid.idelib.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectMavenBuilder {

  private final List<String> goals = new ArrayList<>();
  private final List<String> profiles = new ArrayList<>();
  private final JavaSettings javaSettings;
  private final Directories directories;

  private ProjectProfile profile;

  @Autowired
  public ProjectMavenBuilder(JavaSettings javaSettings, Directories directories) {
    this.javaSettings = javaSettings;
    this.directories = directories;
  }

  public ProjectMavenBuilder goals(String... goals) {
    Collections.addAll(this.goals, goals);
    return this;
  }

  public ProjectMavenBuilder profiles(String... ids) {
    Collections.addAll(profiles, ids);
    return this;
  }

  public ProjectMavenBuilder profile(ProjectProfile profile) {
    this.profile = profile;
    return this;
  }

  public ProcessManager build(Consumer<String> out, Consumer<String> err) throws IOException {
    final List<String> args = new LinkedList<>();
    args.add(javaSettings.getJavaExecutable());
    args.add("-Dmaven.multiModuleProjectDirectory=" + profile.getPath());
    args.add("-Dmaven.home=" + directories.getMaven());
    args.add("-Dclassworlds.conf=" + directories.getMaven().resolve("bin").resolve("m2.conf"));
    args.add("-Dfile.encoding=UTF-8");
    args.add("-cp");
    args.add(directories.getMaven().resolve("boot").resolve("plexus-classworlds-2.5.2.jar").toString());
    args.add("org.codehaus.classworlds.Launcher");
    if (!profiles.isEmpty()) {
      args.add("-P" + String.join(",", profiles));
    }
    args.addAll(goals);

    log(INFO, "Executing {0}", String.join(" ", args));
    final Process process = new ProcessBuilder(args).directory(profile.getPath().toFile()).start();
    return new ProcessManager(profile.getName(), process, out, err);
  }
}
