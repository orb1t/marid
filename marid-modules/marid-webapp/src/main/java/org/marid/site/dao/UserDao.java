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

package org.marid.site.dao;

import org.marid.site.common.Directories;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singleton;

@Repository
public class UserDao implements UserDetailsService {

  private final Directories directories;

  public UserDao(Directories directories) {
    this.directories = directories;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final Path userDir = directories.getProfiles().resolve(username);
    final Path passwordFile = userDir.resolve("password.txt");

    if (Files.isRegularFile(passwordFile)) {
      try {
        return Files.lines(passwordFile, StandardCharsets.ISO_8859_1)
            .findFirst()
            .map(psw -> new User(username, psw.trim(), singleton(new SimpleGrantedAuthority("USER"))))
            .orElseThrow(IllegalStateException::new);
      } catch (Exception x) {
        throw new UsernameNotFoundException("User " + username + " is not found", x);
      }
    } else {
      throw new UsernameNotFoundException("User " + username + " is not found");
    }
  }
}
