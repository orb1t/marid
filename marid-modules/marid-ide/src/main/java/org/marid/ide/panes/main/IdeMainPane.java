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

package org.marid.ide.panes.main;

import javafx.geometry.Side;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.HiddenSidesPane;
import org.marid.ide.logging.IdeLogPane;
import org.marid.ide.tabs.IdeTabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class IdeMainPane extends BorderPane {

  private final HiddenSidesPane pane;
  private final IdeTabPane tabPane;
  private final IdeLogPane ideLogPane;
  private final Preferences preferences;

  @Autowired
  public IdeMainPane(IdeTabPane tabPane, IdeLogPane ideLogPane, IdeToolbar toolbar, Preferences preferences) {
    this.pane = new HiddenSidesPane(tabPane, null, null, ideLogPane, null);
    this.tabPane = tabPane;
    this.ideLogPane = ideLogPane;
    this.preferences = preferences;
    this.ideLogPane.maxHeightProperty().bind(heightProperty().subtract(100.0));
    setTop(toolbar);
    setCenter(pane);
    setFocusTraversable(false);
  }

  public void setPinnedSide(Side side) {
    pane.setPinnedSide(side);
  }
}
