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

import org.jetbrains.annotations.NotNull;

public final class ContextFailEvent extends MaridEvent {

  @NotNull
  private final Throwable cause;

  @NotNull
  private final String beanName;

  public ContextFailEvent(@NotNull MaridRuntime context, @NotNull String beanName, @NotNull Throwable cause) {
    super(context);
    this.beanName = beanName;
    this.cause = cause;
  }

  @NotNull
  public String getBeanName() {
    return beanName;
  }

  @NotNull
  public Throwable getCause() {
    return cause;
  }

  @Override
  public String toString() {
    return super.toString() + "(" + cause + ")";
  }
}
