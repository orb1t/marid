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

package org.marid.app.config;

import org.marid.app.ui.main.MainConfiguration;
import org.marid.app.ui.users.UsersConfiguration;
import org.marid.common.app.endpoint.EndPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EndPointConfiguration {

  @Bean
  public EndPoint mainEndPoint() {
    return new EndPoint("/menu.marid", MainConfiguration.class);
  }

  @Bean
  public EndPoint usersEndPoint() {
    return new EndPoint("/users.marid", UsersConfiguration.class);
  }
}
