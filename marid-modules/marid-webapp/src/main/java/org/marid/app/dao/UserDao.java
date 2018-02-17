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
import org.marid.app.model.MaridUserInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Repository
public class UserDao implements UserDetailsService {

  private final Directories directories;
  private final ObjectMapper mapper;

  public UserDao(Directories directories, ObjectMapper mapper) {
    this.directories = directories;
    this.mapper = mapper;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final Path userDir = directories.getUsers().resolve(username);
    final Path file = userDir.resolve("info.json");

    try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      final MaridUserInfo userInfo = mapper.readValue(reader, MaridUserInfo.class);
      return new MaridUser(username, userInfo);
    } catch (Exception x) {
      throw new UsernameNotFoundException("User " + username + " is not found", x);
    }
  }
}
