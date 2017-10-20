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

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import static org.marid.Ide.FX_SCOPE;

public abstract class ScopedStage extends Stage {

    private final String conversationId;

    public ScopedStage(StageStyle style) {
        super(style);
        conversationId = FX_SCOPE.nextConversationId();
        FX_SCOPE.setConversationId(conversationId);
        focusedProperty().addListener((o, oV, nV) -> {
            if (nV) {
                FX_SCOPE.setConversationId(conversationId);
            }
        });
        addEventHandler(WindowEvent.WINDOW_SHOWING, event -> FX_SCOPE.setConversationId(conversationId));
        addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> FX_SCOPE.destroy(conversationId));
    }
}
