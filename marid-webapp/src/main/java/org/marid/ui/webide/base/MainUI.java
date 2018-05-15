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
package org.marid.ui.webide.base;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Viewport;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.UI;
import org.marid.app.spring.ContextUtils;
import org.marid.app.web.MainServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

@PushStateNavigation
@Push(value = PushMode.AUTOMATIC, transport = Transport.LONG_POLLING)
@Viewport("width=device-width, initial-scale=1")
@Component
public class MainUI extends UI {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainUI.class);

  public MainUI() {
    setNavigator(new Navigator(this, this));
  }

  @Override
  protected void init(VaadinRequest request) {
    getPage().setTitle("Menu");
    getNavigator().navigateTo("a");
  }

  @Override
  public void attach() {
    final AnnotationConfigApplicationContext child = ContextUtils.context(getContext(), c -> {
      c.setId("mainUI");
      c.setDisplayName("mainUI");
      c.registerBean("mainUI", MainUI.class, () -> this);
      getNavigator().addView("a", new Navigator.EmptyView());
      addDetachListener(event -> {
        try {
          c.close();
        } catch (Throwable x) {
          LOGGER.warn("Unable to close {}", c, x);
        }
      });
    });
    super.attach();
    child.refresh();
    child.start();
  }

  @Bean
  public FactoryBean<Navigator> navigator() {
    return new SmartFactoryBean<>() {
      @Override
      public Navigator getObject() {
        return getNavigator();
      }

      @Override
      public Class<?> getObjectType() {
        return Navigator.class;
      }
    };
  }

  @Bean
  public FactoryBean<VaadinSession> vaadinSession() {
    return new SmartFactoryBean<>() {
      @Override
      public VaadinSession getObject() {
        return getSession();
      }

      @Override
      public Class<?> getObjectType() {
        return VaadinSession.class;
      }
    };
  }

  private GenericApplicationContext getContext() {
    final var service = (VaadinServletService) getSession().getService();
    final var servlet = (MainServlet) service.getServlet();
    return servlet.getContext();
  }
}
