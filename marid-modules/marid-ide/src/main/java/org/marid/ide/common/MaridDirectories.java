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

package org.marid.ide.common;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDirectories implements Directories {

  private final Path userHome;
  private final Path marid;
  private final Path profiles;
  private final Path repo;
  private final Path maven;

  public MaridDirectories() {
    userHome = Paths.get(System.getProperty("user.home"));
    marid = userHome.resolve("marid");
    profiles = marid.resolve("profiles");
    repo = marid.resolve("repo");
    maven = marid.resolve("maven");
  }

  @PostConstruct
  private void init() throws IOException {
    Files.createDirectories(profiles);
    Files.createDirectories(repo);
    Files.createDirectories(maven);

    System.setProperty("maven.repo.local", repo.toAbsolutePath().toString());
  }

  @Override
  public Path getUserHome() {
    return userHome;
  }

  @Override
  public Path getMarid() {
    return marid;
  }

  @Override
  public Path getProfiles() {
    return profiles;
  }

  @Override
  public Path getRepo() {
    return repo;
  }

  @Override
  public Path getMaven() {
    return maven;
  }
}
