/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
package org.marid.ui.webide.base;

import org.marid.app.common.Directories;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UserDirectories {

  private final Path userDirectory;
  private final Path projectsDirectory;

  public UserDirectories(Directories directories, CommonProfile profile) throws IOException {
    userDirectory = directories.getUsers().resolve(profile.getEmail());
    projectsDirectory = userDirectory.resolve("projects");

    Files.createDirectories(userDirectory);
    Files.createDirectories(projectsDirectory);
  }

  public Path getUserDirectory() {
    return userDirectory;
  }

  public Path getProjectsDirectory() {
    return projectsDirectory;
  }
}
