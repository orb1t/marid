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

package org.marid.app.model;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

public class MaridUser {

  private final String username;
  private final String password;
  private final boolean enabled;
  private final LocalDate expirationDate;
  private final Set<String> authorities;

  public MaridUser(String username, String password, boolean enabled, LocalDate expirationDate, Set<String> authorities) {
    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.expirationDate = expirationDate;
    this.authorities = authorities;
  }

  public Set<String> getAuthorities() {
    return authorities;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
  }

  public boolean isAccountNonExpired() {
    return expirationDate.compareTo(LocalDate.now(ZoneOffset.UTC)) >= 0;
  }

  public boolean isCredentialsNonExpired() {
    return isAccountNonExpired();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public LocalDate getExpirationDate() {
    return expirationDate;
  }

  public boolean isAdmin() {
    return getAuthorities().contains("ROLE_ADMIN");
  }

  public boolean isUser() {
    return getAuthorities().contains("ROLE_USER");
  }
}
