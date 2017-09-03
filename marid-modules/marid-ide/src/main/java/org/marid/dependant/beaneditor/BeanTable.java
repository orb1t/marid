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
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.dependant.beaneditor.model.SignatureResolver;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.table.MaridTreeTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTreeTableView<BeanData> {

    private final TreeTableColumn<BeanData, String> nameColumn;
    private final TreeTableColumn<BeanData, Label> factoryColumn;
    private final TreeTableColumn<BeanData, String> producerColumn;
    private final TreeTableColumn<BeanData, String> typeColumn;

    @Autowired
    public BeanTable(ProjectProfile profile, SignatureResolver resolver, BeanTableListeners listeners) {
        super(listeners.wrap(profile.getBeanFile()));
        setEditable(true);

        nameColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Name"));
            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(400);
            column.setCellValueFactory(param -> param.getValue().getValue().name);
            column.setCellFactory(param -> new TextFieldTreeTableCell<>(new DefaultStringConverter()));
            column.setEditable(true);
            getColumns().add(column);
        });

        factoryColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Factory"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(600);
            column.setCellValueFactory(param -> {
                final Label label = new Label();
                label.textProperty().bind(resolver.factory(param.getValue().getValue().factory));
                label.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    final String factory = param.getValue().getValue().getFactory();
                    return FontIcons.glyphIcon(factory.contains(".") ? "D_LIBRARY" : "D_LINK");
                }, param.getValue().getValue().factory));
                return new SimpleObjectProperty<>(label);
            });
            getColumns().add(column);
        });

        producerColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Producer"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(700);
            column.setCellValueFactory(param -> resolver.signature(new SimpleObjectProperty<>(param.getValue().getValue())));
            getColumns().add(column);
        });

        typeColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Type"));
            column.setMinWidth(100);
            column.setPrefWidth(400);
            column.setMaxWidth(700);
            getColumns().add(column);
        });

        getRoot().setExpanded(true);
    }

    @Autowired
    private void initActions(List<BeanTableAction> actions) {
        actions().addAll(actions);
    }

    @Autowired
    private void initTypeColumn(SignatureResolver signatureResolver,
                                BeanTypeResolver typeResolver,
                                ProjectProfile profile,
                                AppearanceSettings appearanceSettings) {
        typeColumn.setCellValueFactory(param -> Bindings.createStringBinding(
                () -> {
                    final BeanTypeInfo type = typeResolver.resolve(profile.getBeanContext(), param.getValue().getValue());
                    return signatureResolver.postProcess(type.getType().getTypeName());
                },
                profile.getBeanFile().children,
                appearanceSettings.showFullNamesProperty())
        );
    }

    @Autowired
    @Override
    public void installActions(SpecialActions specialActions) {
        super.installActions(specialActions);
    }
}
