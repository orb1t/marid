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
package org.marid.app.auth;

import io.undertow.security.idm.Account;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class MaridAccount implements Account {

  private final LinkedHashMap<String, CommonProfile> profiles;
  private final Set<String> roles;
  private final Principal principal;

  public MaridAccount(LinkedHashMap<String, CommonProfile> profiles) {
    this.profiles = profiles;
    this.roles = profiles.values().stream().flatMap(p -> p.getRoles().stream()).collect(toSet());
    this.principal = ProfileHelper.flatIntoOneProfile(profiles.values())
        .map(v -> (Principal) v::getId)
        .orElseThrow();
  }

  @Override
  public Principal getPrincipal() {
    return principal;
  }

  @Override
  public Set<String> getRoles() {
    return roles;
  }

  public LinkedHashMap<String, CommonProfile> getProfiles() {
    return profiles;
  }
}
