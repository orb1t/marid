/*-
 * #%L
 * marid-util
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

package org.marid.logging;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

public class MaridLogManager extends LogManager {

  private final AtomicBoolean firstReset = new AtomicBoolean(true);

  @Override
  public void reset() throws SecurityException {
    if (firstReset.compareAndSet(true, false)) {
      super.reset();
    }
  }
}
