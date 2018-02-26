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

package org.marid.app.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;

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

  public MaridUserInfo toInfo(PasswordEncoder passwordEncoder) {
    return new MaridUserInfo(
        "{bcrypt}" + passwordEncoder.encode(password),
        enabled,
        expirationDate.toString(),
        authorities.stream().map(SimpleGrantedAuthority::getAuthority).toArray(String[]::new)
    );
  }
}
