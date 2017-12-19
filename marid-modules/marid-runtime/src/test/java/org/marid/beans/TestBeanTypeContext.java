/*-
 * #%L
 * marid-runtime
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

package org.marid.beans;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

public class TestBeanTypeContext extends BeanTypeContext {

  private final MaridBean bean;

  public TestBeanTypeContext(MaridBean bean) {
    this.bean = bean;
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
        .map(b -> b.getFactory().getType(null, new TestBeanTypeContext(b)))
        .orElseThrow(() -> new NoSuchElementException(name));
  }
}
