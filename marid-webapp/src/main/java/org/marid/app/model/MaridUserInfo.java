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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class MaridUserInfo {

  @JsonProperty
  public final String password;

  @JsonProperty
  public final boolean enabled;

  @JsonProperty
  public final String expirationDate;

  @JsonProperty
  public final Set<String> authorities;

  @JsonCreator
  public MaridUserInfo(@JsonProperty String password,
                       @JsonProperty boolean enabled,
                       @JsonProperty String expirationDate,
                       @JsonProperty String... authorities) {
    this.password = password;
    this.enabled = enabled;
    this.expirationDate = expirationDate;
    this.authorities = Set.of(authorities);
  }

  @Override
  public String toString() {
    return "";
  }
}
