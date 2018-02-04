/*-
 * #%L
 * marid-site
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

package org.marid.site.common;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class Directories {

  private final Path userHome;
  private final Path base;
  private final Path profiles;
  private final Logger logger;

  public Directories(Logger logger) {
    this.logger = logger;
    this.userHome = Paths.get(System.getProperty("user.home"));
    this.base = userHome.resolve("marid-site");
    this.profiles = base.resolve("profiles");
  }

  @PostConstruct
  private void createDirectoriesIfNecessary() throws IOException {
    Files.createDirectories(profiles);
  }

  public Path getUserHome() {
    return userHome;
  }

  public Path getBase() {
    return base;
  }

  public Path getProfiles() {
    return profiles;
  }
}
