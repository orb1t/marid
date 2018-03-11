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
import com.google.common.io.MoreFiles;
import org.marid.app.common.Directories;
import org.marid.app.model.MaridUser;
import org.marid.app.model.MaridUserInfo;
import org.marid.app.model.ModifiedUser;
import org.marid.io.IO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Repository
public class UserDao implements UserDetailsService {

  private final Directories directories;
  private final ObjectMapper mapper;

  public UserDao(Directories directories, ObjectMapper mapper) {
    this.directories = directories;
    this.mapper = mapper;
  }

  @Override
  public MaridUser loadUserByUsername(String username) throws UsernameNotFoundException {
    final Path userDir = directories.getUsers().resolve(username);
    final Path file = userDir.resolve("info.json");

    try (final Reader reader = Files.newBufferedReader(file, UTF_8)) {
      final MaridUserInfo userInfo = mapper.readValue(reader, MaridUserInfo.class);
      return new MaridUser(username, userInfo);
    } catch (Exception x) {
      throw new UsernameNotFoundException("User " + username + " is not found", x);
    }
  }

  public List<MaridUser> getUsers() {
    final Path usersDir = directories.getUsers();

    final List<MaridUser> users = new ArrayList<>();
    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(usersDir, Files::isDirectory)) {
      for (final Path userDir : stream) {
        final Path file = userDir.resolve("info.json");
        try (final Reader reader = Files.newBufferedReader(file, UTF_8)) {
          final MaridUserInfo userInfo = mapper.readValue(reader, MaridUserInfo.class);
          users.add(new MaridUser(userDir.getFileName().toString(), userInfo));
        }
      }
    } catch (Exception x) {
      throw new IllegalStateException(x);
    }

    return users;
  }

  public void saveUser(ModifiedUser user) throws IOException {
    final Path userDir = directories.getUsers().resolve(user.name);
    final Path file = userDir.resolve("info.json");

    if (Files.isRegularFile(file)) {
      try (final IO io = new IO(file)) {
        final MaridUserInfo userInfo = mapper.readValue(io.getInput(), MaridUserInfo.class);
        io.trim();
        mapper.writeValue(io.getOutput(), new MaridUserInfo(userInfo, user));
      }
    } else {
      throw new IllegalStateException("No such user " + user.name);
    }
  }

  public void removeUser(String name) {
    final Path userDir = directories.getUsers().resolve(name);
    try {
      MoreFiles.deleteRecursively(userDir);
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
  }
}
