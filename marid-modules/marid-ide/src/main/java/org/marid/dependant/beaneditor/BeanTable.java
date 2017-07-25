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
import javafx.scene.control.cell.TextFieldTableCell;
import org.marid.dependant.beaneditor.model.MethodSignatureResolver;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.table.MaridTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTableView<BeanData> {

    private final TableColumn<BeanData, String> nameColumn;
    private final TableColumn<BeanData, String> factoryColumn;
    private final TableColumn<BeanData, String> producerColumn;

    @Autowired
    public BeanTable(ProjectProfile profile, MethodSignatureResolver resolver) {
        super(profile.getBeanFile().beans);
        setEditable(true);

        nameColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Name"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(700);
            column.setCellValueFactory(param -> param.getValue().name);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setEditable(true);
            getColumns().add(column);
        });

        factoryColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Factory"));
            column.setMinWidth(200);
            column.setPrefWidth(250);
            column.setMaxWidth(800);
            column.setCellValueFactory(param -> resolver.factory(param.getValue().factory));
            getColumns().add(column);
        });

        producerColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Producer"));
            column.setMinWidth(200);
            column.setPrefWidth(250);
            column.setMaxWidth(800);
            column.setCellValueFactory(param -> resolver.signature(param.getValue().producer));
            getColumns().add(column);
        });
    }

    @Autowired
    private void initActions(@Qualifier("beanTable") List<Function<BeanData, FxAction>> actions) {
        actions().addAll(actions);
    }

    @Autowired
    @Override
    public void installRemoveAction(SpecialAction removeAction) {
        super.installRemoveAction(removeAction);
    }
}
