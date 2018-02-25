/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

package org.marid.app.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.MoreFiles;
import org.marid.app.common.Directories;
import org.marid.app.model.MaridUser;
import org.marid.app.model.MaridUserInfo;
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
      Files.createDirectories(userDir);
      try (final Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
        mapper.writeValue(writer, user.toInfo(new BCryptPasswordEncoder()));
      }
    } catch (IOException x) {
      throw new UncheckedIOException(x);
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
