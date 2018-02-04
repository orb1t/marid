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

package org.marid.runtime.exception;

import org.jetbrains.annotations.NotNull;
import org.marid.cellar.RackContext;
import org.marid.cellar.common.Bottle;

/**
 * @author Dmitry Ovchinnikov
 */
public class BottleInitializationException extends RuntimeException {

  private final RackContext context;
  private final Bottle bottle;

  public BottleInitializationException(@NotNull RackContext context,
                                       @NotNull Bottle bottle,
                                       @NotNull Throwable cause) {
    super(String.format("Bottle %s(%s) initialization exception", bottle, context), cause);
    this.bottle = bottle;
    this.context = context;
  }

  public RackContext getContext() {
    return context;
  }

  public Bottle getBottle() {
    return bottle;
  }
}
