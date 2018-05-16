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
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.IdentityHashMap;

@Component
public class ViewFactory implements ViewProvider {

  private final GenericApplicationContext parent;
  private final Navigator navigator;
  private final HashMap<String, View> views = new HashMap<>();
  private final IdentityHashMap<View, GenericApplicationContext> viewContexts = new IdentityHashMap<>();

  public ViewFactory(GenericApplicationContext parent, Navigator navigator) {
    this.parent = parent;
    this.navigator = navigator;
    this.navigator.addProvider(this);
    this.navigator.addViewChangeListener(event -> {
      return true;
    });
  }

  @Override
  public String getViewName(String viewAndParameters) {
    return views.containsKey(viewAndParameters) ? viewAndParameters : null;
  }

  @Override
  public View getView(String viewName) {
    return views.get(viewName);
  }
}
