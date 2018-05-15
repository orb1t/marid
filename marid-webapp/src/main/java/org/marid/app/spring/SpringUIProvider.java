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
package org.marid.app.spring;

import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinServletService;
import com.vaadin.ui.UI;
import org.marid.app.web.MainServlet;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public class SpringUIProvider extends DefaultUIProvider {

  @Override
  public UI createInstance(UICreateEvent event) {
    final var service = (VaadinServletService) event.getService();
    final var servlet = (MainServlet) service.getServlet();
    final var context = servlet.getContext();
    final var klass = event.getUIClass();
    final var beanFactory = context.getBeanFactory();
    return (UI) beanFactory.createBean(klass, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
  }
}
