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

package org.marid.app.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.marid.app.common.Directories;
import org.marid.app.model.MaridUser;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@Repository
public class UserDao {

  private final Directories directories;
  private final ObjectMapper mapper;

  public UserDao(Directories directories, ObjectMapper mapper) {
    this.directories = directories;
    this.mapper = mapper;
  }

  public MaridUser loadUserByUsername(String username) throws IOException {
    final Path userDir = directories.getUsers().resolve(username);
    final Path file = userDir.resolve("info.json");
    try (final Reader reader = Files.newBufferedReader(file, UTF_8)) {
      final MaridUser user = mapper.readValue(reader, MaridUser.class);
      return new MaridUser(
          userDir.getFileName().toString(),
          user.getPassword(),
          user.isEnabled(),
          user.getExpirationDate(),
          user.getAuthorities()
      );
    }
  }

  public List<MaridUser> getUsers() throws IOException {
    final Path usersDir = directories.getUsers();

    final List<MaridUser> users = new ArrayList<>();
    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(usersDir, Files::isDirectory)) {
      for (final Path userDir : stream) {
        users.add(loadUserByUsername(userDir.getFileName().toString()));
      }
    }

    return users;
  }

  public void saveUser(String username, LocalDate expirationDate, boolean admin, boolean enabled) throws IOException {
    final Path userDir = directories.getUsers().resolve(username);
    final Path file = userDir.resolve("info.json");

    final MaridUser user;
    try (final Reader reader = Files.newBufferedReader(file, UTF_8)) {
      user = mapper.readValue(reader, MaridUser.class);
    }

    try (final Writer writer = Files.newBufferedWriter(file, UTF_8)) {
      final Set<String> authorities;
      if (admin) {
        authorities = new HashSet<>(user.getAuthorities());
        authorities.add("ROLE_ADMIN");
      } else {
        authorities = user.getAuthorities();
      }

      mapper.writeValue(writer, new MaridUser(username, user.getPassword(), enabled, expirationDate, authorities));
    }
  }

  public void removeUser(String name) throws IOException {
    final Path userDir = directories.getUsers().resolve(name);
    FileSystemUtils.deleteRecursively(userDir);
  }
}
