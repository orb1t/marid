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
