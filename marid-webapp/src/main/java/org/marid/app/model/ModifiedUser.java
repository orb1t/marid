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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.marid.app.model.serialize.FormBooleanDeserializer;
import org.marid.app.model.validation.AnotherUser;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

public class ModifiedUser {

  @NotNull
  @JsonProperty
  @AnotherUser(message = "{same.user.modification}")
  public final String name;

  @JsonProperty
  @DateTimeFormat(iso = DATE)
  @NotNull
  @Future
  public final LocalDate date;

  @JsonProperty
  public final boolean admin;

  public ModifiedUser(@JsonProperty String name,
                      @JsonProperty @DateTimeFormat(iso = DATE) LocalDate date,
                      @JsonDeserialize(using = FormBooleanDeserializer.class) @JsonProperty boolean admin) {
    this.name = name;
    this.date = date;
    this.admin = admin;
  }

  public String[] getRoles() {
    return admin
        ? new String[] {"ROLE_USER", "ROLE_ADMIN"}
        : new String[] {"ROLE_USER"};
  }
}
