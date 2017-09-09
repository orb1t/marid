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

package org.marid.dependant.beaneditor.initializers;

import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.marid.dependant.beaneditor.BeanTable;
import org.marid.dependant.beaneditor.model.SignatureResolver;
import org.marid.ide.model.BeanMethodData;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.table.MaridListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javafx.scene.paint.Color.TRANSPARENT;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanInitializerList extends MaridListView<BeanMethodData> {

    @Autowired
    public BeanInitializerList(SignatureResolver signatureResolver) {
        setBorder(new Border(new BorderStroke(TRANSPARENT, BorderStrokeStyle.NONE, null, null)));
        setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        cellSupplier = () -> {
            final ListCell<BeanMethodData> cell = new ListCell<>();
            cell.textProperty().bind(signatureResolver.signature(cell.itemProperty()));
            return cell;
        };
    }

    @Autowired
    private void initOnSelectionListener(BeanTable table) {
        table.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> {
            if (nV == null) {
                setItems(FXCollections.observableArrayList());
            } else {
                setItems(nV.getValue().initializers);
            }
        });
    }

    @Override
    @Autowired
    public void installActions(SpecialActions specialActions) {
        super.installActions(specialActions);
    }
}
