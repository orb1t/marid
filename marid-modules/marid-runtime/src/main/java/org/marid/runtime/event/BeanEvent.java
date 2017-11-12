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

package org.marid.runtime.event;

import org.marid.runtime.context.MaridRuntime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanEvent extends MaridEvent {

  @Nonnull
  private final String name;

  @Nullable
  private final Object bean;

  @Nonnull
  private final String type;

  public BeanEvent(@Nullable MaridRuntime source, @Nonnull String name, @Nullable Object bean, @Nonnull String type) {
    super(source);
    this.name = name;
    this.bean = bean;
    this.type = type;
  }

  @Nullable
  public Object getBean() {
    return bean;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("%s[%s,%s]", super.toString(), type, name);
  }
}
