/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.idefx.beans;

import org.jetbrains.annotations.NotNull;
import org.marid.beans.BeanTypeContext;
import org.marid.beans.MaridBean;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IdeBeanContext extends BeanTypeContext {

  private final MaridBean bean;
  private final ClassLoader classLoader;
  private final List<Throwable> errors = new ArrayList<>();

  public IdeBeanContext(MaridBean bean, ClassLoader classLoader) {
    this.bean = bean;
    this.classLoader = classLoader;
  }

  @NotNull
  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public <E extends Throwable> void throwError(@NotNull E exception) throws E {
    errors.add(exception);
  }

  @NotNull
  @Override
  public MaridBean getBean() {
    return bean;
  }

  @NotNull
  @Override
  public Type getBeanType(@NotNull String name) {
    return bean.matchingCandidates()
        .filter(b -> b.getName().equals(name))
        .findFirst()
        .map(b -> b.getFactory().getType(null, new IdeBeanContext((IdeBean) b, classLoader)))
        .orElseGet(() -> {
          errors.add(new IllegalStateException("Unable to get type of the bean " + name));
          return Object.class;
        });
  }
}
