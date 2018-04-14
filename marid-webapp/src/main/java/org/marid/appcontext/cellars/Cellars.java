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

package org.marid.appcontext.cellars;

import org.marid.appcontext.session.SessionDirectory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class Cellars {

  private final Path directory;

  public Cellars(SessionDirectory directory) throws IOException {
    this.directory = directory.getDirectory().resolve("cellars");
    Files.createDirectories(this.directory);
  }

  public Path getDirectory() {
    return directory;
  }

  public void add(String name) {
    Objects.requireNonNull(name, "Cellar name cannot be null");

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Cellar name cannot be empty");
    }

    if (!name.codePoints().allMatch(Character::isUnicodeIdentifierPart)) {
      throw new IllegalArgumentException("Invalid cellar name: " + name);
    }

    final Path path = directory.resolve(name);

    try {
      Files.createDirectories(path);
    } catch (IOException x) {
      throw new UncheckedIOException(x.getMessage(), x);
    }
  }

  public void delete(String name) {
    Objects.requireNonNull(name, "Cellar name cannot be null");

    if (name.isEmpty()) {
      throw new IllegalArgumentException("Cellar name cannot be empty");
    }

    if (!name.codePoints().allMatch(Character::isUnicodeIdentifierPart)) {
      throw new IllegalArgumentException("Invalid cellar name: " + name);
    }

    final Path path = directory.resolve(name);

    try {
      FileSystemUtils.deleteRecursively(path);
    } catch (IOException x) {
      throw new UncheckedIOException(x.getMessage(), x);
    }
  }

  public List<String> cellars() {
    try (final Stream<Path> stream = Files.list(directory).filter(Files::isDirectory)) {
      return stream.map(Path::getFileName).map(Object::toString).collect(Collectors.toList());
    } catch (IOException x) {
      throw new UncheckedIOException(x.getMessage(), x);
    }
  }
}
