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
package org.marid.app.web;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import org.marid.app.annotation.PrototypeScoped;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

@Component
@PrototypeScoped
public class MainServlet extends VaadinServlet {

  private final GenericApplicationContext context;

  public MainServlet(GenericApplicationContext context) {
    this.context = context;
  }

  @Override
  protected VaadinServletService createServletService(DeploymentConfiguration conf) throws ServiceException {
    final MainServletService servletService = new MainServletService(context, this, conf);
    servletService.init();
    return servletService;
  }
}
