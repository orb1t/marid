/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.props;

import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.jfx.LocalizedStrings;

import java.util.LinkedList;

import static java.lang.Runtime.getRuntime;

@Tag("manual")
class LocalizedStringHeapTest {

  private final LinkedList<ObservableValue<String>> observableValues = new LinkedList<>();

  @Test
  void testOutOfMemory() {
    for (int i = 0; i < 100_000; i++) {
      for (int j = 0; j < 1_000; j++) {
        observableValues.add(LocalizedStrings.ls("str %d", j));
      }
      Thread.yield();
      observableValues.clear();
      System.out.printf("%d: %d/%d%n", i, getRuntime().freeMemory(), getRuntime().totalMemory());
    }
  }
}
