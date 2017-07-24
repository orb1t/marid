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

import javafx.scene.control.ListCell;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import org.marid.dependant.beaneditor.BeanTable;
import org.marid.ide.model.BeanMethodData;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.jfx.table.MaridListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.scene.paint.Color.TRANSPARENT;
import static org.marid.ide.model.BeanMethodData.signature;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanInitializerList extends MaridListView<BeanMethodData> {

    @Autowired
    public BeanInitializerList(AppearanceSettings appearanceSettings) {
        setBorder(new Border(new BorderStroke(TRANSPARENT, BorderStrokeStyle.NONE, null, null)));
        cellSupplier.set(() -> {
            final ListCell<BeanMethodData> cell = new ListCell<>();
            cell.textProperty().bind(createStringBinding(() -> {
                if (cell.getItem() == null) {
                    return null;
                } else {
                    return signature(cell.getItem().getSignature(), appearanceSettings.showFullNamesProperty().get());
                }
            }, cell.itemProperty(), appearanceSettings.showFullNamesProperty()));
            return cell;
        });
    }

    @Autowired
    private void initOnSelectionListener(BeanTable table) {
        table.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> {
            if (nV == null) {
                getItems().clear();
            } else {
                getItems().setAll(nV.initializers);
            }
        });
    }
}
