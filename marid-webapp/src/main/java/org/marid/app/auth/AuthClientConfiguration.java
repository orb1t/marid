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

import org.marid.app.props.GoogleAuthProperties;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GoogleAuthProperties.class})
public class AuthClientConfiguration {

  @Bean
  public GoogleOidcClient googleOidcClient(GoogleAuthProperties properties) {
    final OidcConfiguration conf = new OidcConfiguration();
    conf.setClientId(properties.getClientId());
    conf.setSecret(properties.getSecret());
    conf.addCustomParam("prompt", "consent");
    conf.setUseNonce(true);

    final GoogleOidcClient client = new GoogleOidcClient(conf);
    return client;
  }

  @Bean
  public Clients authClients(Client<?, ?>[] clients, GoogleOidcClient googleOidcClient) {
    final Clients authClients = new Clients("/callback", clients);
    authClients.setDefaultClient(googleOidcClient);
    authClients.addAuthorizationGenerator((context, profile) -> {
      profile.addRole("ROLE_USER");
      return profile;
    });
    return authClients;
  }

  @Bean
  public Config authConfig(Clients authClients) {
    final Config config = new Config(authClients);
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
    config.addAuthorizer("user", new RequireAnyRoleAuthorizer("ROLE_USER"));
    return config;
  }
}
