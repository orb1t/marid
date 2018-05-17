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

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import org.marid.applib.view.StaticView;
import org.marid.applib.view.ViewName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Component
public class StaticViewFactory implements ViewProvider {

  private final Map<String, Supplier<StaticView>> views;

  public StaticViewFactory(GenericApplicationContext context) {
    views = Stream.of(context.getBeanNamesForType(StaticView.class))
        .map(name -> {
          final ViewName viewName = context.findAnnotationOnBean(name, ViewName.class);
          return viewName == null ? new String[] {name, null} : new String[] {name, viewName.value()};
        })
        .filter(e -> e[1] != null)
        .collect(toMap(e -> e[1], e -> () -> context.getBean(e[0], StaticView.class)));
  }

  @Autowired
  private void initNavigator(Navigator navigator) {
    navigator.addProvider(this);
  }

  @Override
  public String getViewName(String viewAndParameters) {
    return views.containsKey(viewAndParameters) ? viewAndParameters : null;
  }

  @Override
  public View getView(String viewName) {
    return views.getOrDefault(viewName, () -> null).get();
  }
}
