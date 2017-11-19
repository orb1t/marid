/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
