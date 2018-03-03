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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
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
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
          final MaridUserInfo userInfo = mapper.readValue(reader, MaridUserInfo.class);
          users.add(new MaridUser(userDir.getFileName().toString(), userInfo));
        }
      }
    } catch (Exception x) {
      throw new IllegalStateException(x);
    }

    return users;
  }

  public void saveUser(MaridUser user) {
    final Path userDir = directories.getUsers().resolve(user.getUsername());
    final Path file = userDir.resolve("info.json");

    try {
      final MaridUserInfo info;
      if (Files.isRegularFile(file)) {
        final MaridUserInfo userInfo;
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
          userInfo = mapper.readValue(reader, MaridUserInfo.class);
        }
        info = new MaridUserInfo(
            userInfo.password,
            user.isEnabled(),
            user.getExpirationDate().toString(),
            user.getAuthorities().stream().map(SimpleGrantedAuthority::getAuthority).toArray(String[]::new)
        );
      } else {
        Files.createDirectories(userDir);
        info = user.toInfo(new BCryptPasswordEncoder());
      }
      try (final Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        mapper.writeValue(writer, info);
      }
    } catch (Exception x) {
      throw new IllegalStateException(x);
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
