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

package org.marid.ide.tabs;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class IdeTabKey {

  public final ObservableValue<String> textBinding;
  public final Supplier<Node> graphicBinding;

  public IdeTabKey(ObservableValue<String> textBinding, Supplier<Node> graphicBinding) {
    this.textBinding = textBinding;
    this.graphicBinding = graphicBinding;
  }

  @Override
  public int hashCode() {
    return textBinding.getValue().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    final IdeTabKey that = (IdeTabKey) obj;
    return this.textBinding.getValue().equals(that.textBinding.getValue());
  }
}
