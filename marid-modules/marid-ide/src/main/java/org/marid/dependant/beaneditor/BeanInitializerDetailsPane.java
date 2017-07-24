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

import javafx.beans.binding.Bindings;
import javafx.geometry.Side;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;
import org.controlsfx.control.MasterDetailPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanInitializerDetailsPane extends MasterDetailPane {

    final BeanInitializerList initializerList;
    final BeanInitializerArgTable argTable;

    @Autowired
    public BeanInitializerDetailsPane(BeanInitializerList initializerList, BeanInitializerArgTable argTable) {
        super(Side.BOTTOM, initializerList, argTable, false);
        this.initializerList = initializerList;
        this.argTable = argTable;
        setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.NONE, null, null)));
        initializerList.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> {
            if (nV == null) {
                showDetailNodeProperty().unbind();
                setShowDetailNode(false);
            } else {
                showDetailNodeProperty().bind(Bindings.isEmpty(nV.args).not());
            }
        });
    }
}
