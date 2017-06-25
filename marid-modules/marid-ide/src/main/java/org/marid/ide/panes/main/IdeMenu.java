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

package org.marid.ide.panes.main;

import javafx.scene.control.MenuBar;
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
public class IdeMenu extends MenuBar {

    private final ObjectFactory<Map<String, FxAction>> menuActionsFactory;

    @Autowired
    public IdeMenu(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        this.menuActionsFactory = menuActionsFactory;
    }

    @EventListener
    private void onIdeStart(ContextStartedEvent event) {
        getMenus().addAll(MaridActions.menus(menuActionsFactory.getObject()));
    }
}
