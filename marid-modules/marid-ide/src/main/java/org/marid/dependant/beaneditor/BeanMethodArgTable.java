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

import javafx.scene.control.TableColumn;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.jfx.table.MaridTableView;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class BeanMethodArgTable extends MaridTableView<BeanMethodArgData> {

    protected final TableColumn<BeanMethodArgData, String> nameColumn;
    protected final TableColumn<BeanMethodArgData, String> typeColumn;
    protected final TableColumn<BeanMethodArgData, String> valueColumn;

    public BeanMethodArgTable() {
        setEditable(false);

        nameColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Name"));
            column.setMinWidth(70);
            column.setPrefWidth(80);
            column.setPrefWidth(350);
            column.setCellValueFactory(param -> param.getValue().name);
            getColumns().add(column);
        });

        typeColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Type"));
            column.setMinWidth(70);
            column.setPrefWidth(80);
            column.setPrefWidth(350);
            column.setCellValueFactory(param -> param.getValue().type);
            getColumns().add(column);
        });

        valueColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Value"));
            column.setMinWidth(200);
            column.setPrefWidth(400);
            column.setPrefWidth(1000);
            column.setCellValueFactory(param -> param.getValue().value);
            getColumns().add(column);
        });
    }
}
