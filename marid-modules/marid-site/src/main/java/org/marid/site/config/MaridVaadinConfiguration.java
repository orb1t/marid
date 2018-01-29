/*-
 * #%L
 * marid-site
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

package org.marid.site.config;

import com.vaadin.spring.annotation.EnableVaadinNavigation;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.boot.annotation.EnableVaadinServlet;
import com.vaadin.spring.internal.*;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.declarative.Design;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
@EnableVaadinNavigation
@EnableVaadinServlet
public class MaridVaadinConfiguration {

  @Bean
  public static VaadinSessionScope vaadinSessionScope() {
    return new VaadinSessionScope();
  }

  @Bean
  public static UIScopeImpl uIScope() {
    return new UIScopeImpl();
  }

  @Bean
  @UIScope
  public SpringViewProvider viewProvider(GenericApplicationContext context) {
    return new SpringViewProvider(context, context);
  }

  @Bean
  @UIScope
  public ViewCache viewCache() {
    return new DefaultViewCache();
  }

  @Bean
  public VaadinSpringComponentFactory componentFactory() {
    final VaadinSpringComponentFactory factory = new VaadinSpringComponentFactory();
    Design.setComponentFactory(factory);
    return factory;
  }
}
