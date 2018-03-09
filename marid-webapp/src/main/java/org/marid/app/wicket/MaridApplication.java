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

package org.marid.app.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Service
public class MaridApplication extends WebApplication
    implements IRoleCheckingStrategy, IUnauthorizedComponentInstantiationListener {

  @Override
  public Class<? extends Page> getHomePage() {
    return MaridHome.class;
  }

  @Override
  public Session newSession(Request request, Response response) {
    return new MaridWebSession(request);
  }

  @Override
  protected void init() {
    super.init();

    final ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());

    getComponentInstantiationListeners().add(new MaridComponentInjector(context));
    getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(this));
    getSecuritySettings().setUnauthorizedComponentInstantiationListener(this);

    mountPage("/users/users.marid", MaridHome.class);
  }

  @Override
  public boolean hasAnyRole(Roles roles) {
    final Roles sessionRoles = AbstractAuthenticatedWebSession.get().getRoles();
    return (sessionRoles != null) && sessionRoles.hasAnyRole(roles);
  }

  @Override
  public void onUnauthorizedInstantiation(Component component) {
    throw new UnauthorizedInstantiationException(component.getClass());
  }
}
