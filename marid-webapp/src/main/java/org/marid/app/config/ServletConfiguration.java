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
package org.marid.app.config;

import com.vaadin.server.VaadinServlet;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import org.marid.app.spring.SpringUIProvider;
import org.marid.app.ui.MainUI;
import org.marid.app.web.AuthFilter;
import org.marid.app.web.MainServlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static com.vaadin.server.Constants.SERVLET_PARAMETER_PRODUCTION_MODE;
import static com.vaadin.server.Constants.SERVLET_PARAMETER_UI_PROVIDER;
import static com.vaadin.server.VaadinSession.UI_PARAMETER;

@Component
public class ServletConfiguration {

  @Bean
  public ServletInfo servletInfo(ObjectProvider<MainServlet> mainServletProvider,
                                 @Value("${vaadin.production:true}") boolean production) {
    final var info = new ServletInfo("vaadinServlet", VaadinServlet.class, null);
    info.setAsyncSupported(true);
    info.setLoadOnStartup(1);
    info.setEnabled(true);
    info.addMappings("/app/*", "/VAADIN/*");
    info.setInstanceFactory(() -> new ImmediateInstanceHandle<>(mainServletProvider.getObject()));
    info.addInitParam(SERVLET_PARAMETER_UI_PROVIDER, SpringUIProvider.class.getName());
    info.addInitParam(SERVLET_PARAMETER_PRODUCTION_MODE, Boolean.toString(production));
    info.addInitParam(UI_PARAMETER, MainUI.class.getName());
    return info;
  }

  @Bean
  public FilterInfo authFilterInfo(ObjectProvider<AuthFilter> authFilterProvider) {
    final var info = new FilterInfo("authFilter", AuthFilter.class, null);
    info.setAsyncSupported(true);
    info.setInstanceFactory(() -> new ImmediateInstanceHandle<>(authFilterProvider.getObject()));
    return info;
  }
}
