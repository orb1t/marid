/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.exception;

import org.marid.cellar.RackContext;

import java.util.LinkedList;

public class RackContextCloseException extends Exception {

  private final RackContext context;

  public RackContextCloseException(RackContext context, LinkedList<Throwable> errors) {
    super(String.format("Context %s close exception", context), errors.size() == 1 ? errors.getFirst() : null);
    this.context = context;

    if (errors.size() > 1) {
      errors.forEach(this::addSuppressed);
    }
  }

  public RackContext getContext() {
    return context;
  }
}
