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

package org.marid.dependant.beaneditor;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.marid.jfx.icons.FontIcons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.selectBoolean;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanDetailsPane extends TabPane {

    private final Tab argsTab;
    private final Tab initializersTab;

    public BeanDetailsPane() {
        argsTab = build(new Tab(), tab -> {
            tab.textProperty().bind(ls("Arguments"));
            tab.setGraphic(FontIcons.glyphIcon("D_DISQUS", 16));
            getTabs().add(tab);
        });

        initializersTab = build(new Tab(), tab -> {
            tab.textProperty().bind(ls("Initializers"));
            tab.setGraphic(FontIcons.glyphIcon("D_STAR", 16));
            getTabs().add(tab);
        });
    }

    @Autowired
    private void initArgsPane(BeanArgTable argTable) {
        argsTab.setContent(argTable);
        argsTab.disableProperty().bind(selectBoolean(argTable.itemsProperty(), "empty"));
    }

    @Autowired
    private void initInitializersPane(BeanInitializerDetailsPane pane, BeanTable table) {
        initializersTab.setContent(pane);
        initializersTab.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    }
}
