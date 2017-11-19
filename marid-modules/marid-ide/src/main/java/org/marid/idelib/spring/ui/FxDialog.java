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

package org.marid.idelib.spring.ui;

import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;

import static org.marid.Ide.FX_SCOPE;

public abstract class FxDialog<R> extends Dialog<R> {

  private final String conversationId;

  public FxDialog() {
    this.conversationId = FX_SCOPE.nextConversationId();
    FX_SCOPE.setConversationId(conversationId);
    setOnShowing(event -> FX_SCOPE.setConversationId(conversationId));
    setOnHidden(event -> FX_SCOPE.destroy(conversationId));
    getDialogPane().focusedProperty().addListener((o, oV, nV) -> {
      if (nV) {
        FX_SCOPE.setConversationId(conversationId);
      }
    });
  }

  public void addOnShowing(EventHandler<DialogEvent> eventHandler) {
    final EventHandler<DialogEvent> old = getOnShowing();
    setOnShowing(event -> {
      old.handle(event);
      eventHandler.handle(event);
    });
  }

  public void addOnHidden(EventHandler<DialogEvent> eventHandler) {
    final EventHandler<DialogEvent> old = getOnHidden();
    setOnHidden(event -> {
      old.handle(event);
      eventHandler.handle(event);
    });
  }
}
