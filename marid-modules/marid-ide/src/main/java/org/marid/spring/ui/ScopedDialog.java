/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.spring.ui;

import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;

import static org.marid.Ide.FX_SCOPE;

public abstract class ScopedDialog<R> extends Dialog<R> {

    private final String conversationId;

    public ScopedDialog() {
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
