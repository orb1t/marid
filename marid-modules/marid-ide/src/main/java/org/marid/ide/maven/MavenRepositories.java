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

package org.marid.ide.maven;

import org.marid.ide.common.Directories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class MavenRepositories {

  private static final String MAVEN_URL = "http://www.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.zip";

  private final Path baseDir;
  private final File centralDir;
  private final List<MavenRepository> repositories = new ArrayList<>();

  @Autowired
  public MavenRepositories(Directories directories) {
    baseDir = directories.getMarid().resolve("cache").resolve("repos");
    centralDir = baseDir.resolve("central").resolve("cache").toFile();

    // maven central
    repositories.add(new MavenRepository("central", centralDir, "http://repo1.maven.org/maven2"));

    // local repository
    repositories.add(new MavenRepository("local", directories.getRepo().toFile(), null));
  }

  public Path getBaseDir() {
    return baseDir;
  }

  public List<MavenRepository> getRepositories() {
    return repositories;
  }

  @Autowired
  private void initMaven(Directories directories) {
    final Path mavenHome = directories.getMaven();
    final Pattern slashPattern = Pattern.compile("/");

    try {
      if (Files.notExists(mavenHome) || Files.list(mavenHome).count() == 0) {
        final URL mavenZip = new URL(MAVEN_URL);
        try (final ZipInputStream zis = new ZipInputStream(mavenZip.openStream())) {
          for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
            if (e.isDirectory()) {
              continue;
            }
            try {
              final Path path = slashPattern.splitAsStream(e.getName())
                  .skip(1L)
                  .reduce(mavenHome, Path::resolve, (p1, p2) -> p2);
              final Path dir = path.getParent();
              Files.createDirectories(dir);
              Files.copy(zis, path, REPLACE_EXISTING);
              log(INFO, "Copied {0} to {1}", e.getName(), path);
            } finally {
              zis.closeEntry();
            }
          }
        }
      }
    } catch (Exception x) {
      log(WARNING, "Unable to initialize maven", x);
    }
  }
}
