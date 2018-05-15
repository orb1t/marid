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

import com.vaadin.server.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;

@Component
public class MainServlet extends VaadinServlet implements BootstrapListener {

  private final GenericApplicationContext context;

  public MainServlet(GenericApplicationContext context) {
    this.context = context;
  }

  @Override
  protected VaadinServletService createServletService() throws ServletException, ServiceException {
    final var service = super.createServletService();
    service.addSessionInitListener((SessionInitListener) event -> event.getSession().addBootstrapListener(this));
    return service;
  }

  @Override
  public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
  }

  @Override
  public void modifyBootstrapPage(BootstrapPageResponse response) {
    response.getDocument().head().getElementsByAttributeValue("rel", "icon").attr("href", "/public/marid32.png");
  }

  public GenericApplicationContext getContext() {
    return context;
  }
}
