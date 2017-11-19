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

package org.marid.ide.logging;

import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.logging.LogComponent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Double.max;
import static java.lang.Double.min;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class IdeLogPane extends BorderPane {

  private final Separator divider;
  private final LogComponent component = new LogComponent(IdeLogHandler.LOG_RECORDS);

  public IdeLogPane() {
    setTop(divider = new Separator(Orientation.HORIZONTAL));
    setMinHeight(100);
    setPrefHeight(250);

    final AtomicReference<Double> lastPos = new AtomicReference<>(0.0);
    final AtomicReference<Double> lastHeight = new AtomicReference<>(0.0);
    divider.setOnMouseEntered(event -> {
      divider.getScene().setCursor(Cursor.V_RESIZE);
      lastPos.set(event.getScreenY());
      lastHeight.set(getHeight());
    });
    divider.setOnMouseExited(event -> divider.getScene().setCursor(Cursor.DEFAULT));
    divider.setOnMouseDragged(event -> {
      final double delta = event.getScreenY() - lastPos.get();
      setPrefHeight(min(getMaxHeight(), max(getMinHeight(), lastHeight.get() - delta)));
      getParent().requestLayout();
    });
  }

  @EventListener
  public void onContextStart(ContextStartedEvent event) {
    setCenter(component);
    component.scrollTo(component.getItems().size() - 1);
  }
}
