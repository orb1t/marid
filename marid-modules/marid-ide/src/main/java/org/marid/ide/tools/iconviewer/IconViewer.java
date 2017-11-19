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

package org.marid.ide.tools.iconviewer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10n;
import org.marid.idelib.spring.annotation.PrototypeComponent;
import org.marid.idelib.spring.ui.FxStage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class IconViewer extends FxStage {

  public IconViewer() {
    super(StageStyle.DECORATED);
  }

  @Autowired
  private void configure(IdePane idePane, IconViewerTable table) {
    initOwner(idePane.getScene().getWindow());
    setTitle(L10n.s("Icon viewer"));
    final BorderPane pane = new BorderPane(table);
    pane.setPrefSize(800, 600);
    final Label countLabel = new Label(L10n.s("Icon count: %d", table.getItems().size()));
    pane.setBottom(countLabel);
    BorderPane.setMargin(table, new Insets(10, 10, 5, 10));
    BorderPane.setMargin(countLabel, new Insets(5, 10, 10, 10));
    setScene(new Scene(pane));
  }
}
