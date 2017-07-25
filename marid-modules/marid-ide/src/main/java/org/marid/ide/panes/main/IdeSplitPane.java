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
public class IdeSplitPane extends HiddenSidesPane {

    private final IdeTabPane tabPane;
    private final IdeLogPane ideLogPane;
    private final Preferences preferences;

    @Autowired
    public IdeSplitPane(IdeTabPane tabPane, IdeLogPane ideLogPane, Preferences preferences) {
        super(tabPane, null, null, ideLogPane, null);
        this.tabPane = tabPane;
        this.ideLogPane = ideLogPane;
        this.preferences = preferences;
        setFocusTraversable(false);
    }
}
