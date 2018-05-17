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
import org.marid.applib.spring.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

import static org.marid.applib.spring.ContextUtils.closeListener;

@Component
public class ViewFactory implements ViewProvider {

  private final Cleaner refCleaner;
  private final HashMap<String, WeakReference<View>> views = new HashMap<>();
  private final HashMap<ApplicationContext, HashSet<GenericApplicationContext>> contexts = new HashMap<>();

  public ViewFactory(Cleaner refCleaner) {
    this.refCleaner = refCleaner;
  }

  @Override
  public String getViewName(String viewAndParameters) {
    final WeakReference<View> viewRef = views.get(viewAndParameters);
    return viewRef == null ? null : (viewRef.get() == null ? null : viewAndParameters);
  }

  @Override
  public View getView(String viewName) {
    final WeakReference<View> viewRef = views.get(viewName);
    return viewRef == null ? null : viewRef.get();
  }

  @Autowired
  private void initNavigator(Navigator navigator) {
    navigator.addProvider(this);
  }

  public <V extends View> void show(String path, Class<V> view, Supplier<V> factory, GenericApplicationContext parent) {
    final var ctx = ContextUtils.context(parent, c -> {
      c.setId(path);
      c.setDisplayName(path);
      c.registerBean(path, view, factory);
      c.refresh();
      c.start();
    });
    contexts.computeIfAbsent(parent, k -> new HashSet<>()).add(ctx);
    final var instance = ctx.getBean(path, view);
    views.put(path, new WeakReference<>(instance));
    refCleaner.register(instance.getViewComponent(), () -> {
      if (!contexts.containsKey(ctx)) {
        ctx.close();
      }
    });
    ctx.addApplicationListener(closeListener(ctx, event -> {
      views.remove(path);
      contexts.computeIfPresent(parent, (k, old) -> {
        old.remove(ctx);
        return old.isEmpty() ? null : old;
      });
    }));
  }
}
