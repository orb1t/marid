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
