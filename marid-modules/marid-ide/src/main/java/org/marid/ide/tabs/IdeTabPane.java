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

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeTabPane extends TabPane {

  private static final Object TAB_KEY = new Object();

  public IdeTabPane() {
    setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
    setFocusTraversable(false);
  }

  public void addTab(@Nonnull Object key, @Nonnull Supplier<? extends Tab> tabSupplier) {
    for (int i = getTabs().size() - 1; i >= 0; i--) {
      final Object v = getTabs().get(i).getProperties().get(TAB_KEY);
      if (v != null && v.equals(key)) {
        getSelectionModel().select(i);
        return;
      }
    }

    final Tab tab = tabSupplier.get();
    tab.getProperties().put(TAB_KEY, key);
    getTabs().add(tab);
  }
}
