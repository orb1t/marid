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

package org.marid.ide.development;

import org.marid.ide.common.Directories;
import org.marid.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
@Profile("development")
@Component
public class MavenRepositoryCopier {

  @Autowired
  public void init(Directories directories) throws IOException {
    final Path m2Repo = directories.getUserHome().resolve(".m2").resolve("repository");
    if (Files.isDirectory(m2Repo)) {
      final Path orgMarid = Paths.get("org", "marid");
      Files.walkFileTree(m2Repo, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if (!dir.equals(m2Repo)) {
            final Path relative = m2Repo.relativize(dir);
            if (relative.startsWith(orgMarid)) {
              final Path dest = directories.getRepo().resolve(relative);
              Files.createDirectories(dest);
            } else if (relative.getNameCount() >= 2) {
              return FileVisitResult.SKIP_SUBTREE;
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          final Path relative = m2Repo.relativize(file);
          final Path dest = directories.getRepo().resolve(relative);
          Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
          Log.log(Level.INFO, "Copied {0} to {1}", file.getFileName(), dest.getParent());
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
}
