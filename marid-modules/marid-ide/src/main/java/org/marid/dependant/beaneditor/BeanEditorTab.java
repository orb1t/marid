package org.marid.dependant.beaneditor;

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

import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import org.marid.ide.model.TextFile;
import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTab extends IdeTab {

    @Autowired
    public BeanEditorTab(SplitPane beanSplitPane,
                         ObservableStringValue beanEditorTabText,
                         Supplier<Node> beanEditorGraphic,
                         TextFile javaFile) {
        super(beanSplitPane, beanEditorTabText, beanEditorGraphic);
        addNodeObservables(javaFile.path);
    }
}
