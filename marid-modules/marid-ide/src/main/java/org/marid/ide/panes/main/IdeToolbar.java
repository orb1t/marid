/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.ide.panes.main;

import javafx.scene.control.ToolBar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar {

    private final ObjectFactory<Map<String, FxAction>> menuActionsFactory;

    @Autowired
    public IdeToolbar(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        this.menuActionsFactory = menuActionsFactory;
    }

    @EventListener
    private void onIdeStart(ContextStartedEvent event) {
        MaridActions.initToolbar(menuActionsFactory.getObject(), this);
    }
}
