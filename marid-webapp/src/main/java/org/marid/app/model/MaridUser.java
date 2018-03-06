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

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.util.stream.Collectors.toSet;

public class MaridUser implements UserDetails {

  private final String username;
  private final String password;
  private final boolean enabled;
  private final LocalDate expirationDate;
  private final Set<SimpleGrantedAuthority> authorities;

  public MaridUser(String username, MaridUserInfo userInfo) {
    this.username = username;
    this.password = userInfo.password;
    this.enabled = userInfo.enabled;
    this.expirationDate = LocalDate.parse(userInfo.expirationDate);
    this.authorities = userInfo.authorities.stream().map(SimpleGrantedAuthority::new).collect(toSet());
  }

  @Override
  public Collection<SimpleGrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return expirationDate.compareTo(LocalDate.now(ZoneOffset.UTC)) >= 0;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isAccountNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public LocalDate getExpirationDate() {
    return expirationDate;
  }

  public boolean isAdmin() {
    return getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  public boolean isUser() {
    return getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public MaridUserInfo toInfo(UnaryOperator<String> passwordEncoder) {
    return new MaridUserInfo(
        "{bcrypt}" + passwordEncoder.apply(password),
        enabled,
        expirationDate.toString(),
        authorities.stream().map(SimpleGrantedAuthority::getAuthority).toArray(String[]::new)
    );
  }
}
