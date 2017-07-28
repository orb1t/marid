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

package org.marid.ide.status;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeServiceButton extends Button {

    final Label label = new Label();
    final HBox box = new HBox(5, label);

    public IdeServiceButton() {
        setGraphic(box);

        HBox.setHgrow(label, Priority.ALWAYS);

        label.setMaxHeight(Double.MAX_VALUE);
        box.setMaxHeight(Double.MAX_VALUE);
    }
}
