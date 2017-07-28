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

import com.google.common.reflect.TypeToken;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import org.marid.dependant.beaneditor.model.SignatureResolver;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.icons.FontIcons;
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
    private final TableColumn<BeanData, Label> factoryColumn;
    private final TableColumn<BeanData, String> producerColumn;
    private final TableColumn<BeanData, String> typeColumn;

    @Autowired
    public BeanTable(ProjectProfile profile, SignatureResolver resolver) {
        super(profile.getBeanFile().beans);
        setEditable(true);

        nameColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Name"));
            column.setMinWidth(100);
            column.setPrefWidth(100);
            column.setMaxWidth(400);
            column.setCellValueFactory(param -> param.getValue().name);
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setEditable(true);
            getColumns().add(column);
        });

        factoryColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Factory"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(600);
            column.setCellValueFactory(param -> {
                final Label label = new Label();
                label.textProperty().bind(resolver.factory(param.getValue().factory));
                label.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    final String factory = param.getValue().getFactory();
                    return FontIcons.glyphIcon(factory.contains(".") ? "D_LIBRARY" : "D_LINK");
                }, param.getValue().factory));
                return new SimpleObjectProperty<>(label);
            });
            getColumns().add(column);
        });

        producerColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Producer"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(700);
            column.setCellValueFactory(param -> resolver.signature(param.getValue().producer));
            getColumns().add(column);
        });

        typeColumn = build(new TableColumn<>(), column -> {
            column.textProperty().bind(ls("Type"));
            column.setMinWidth(100);
            column.setPrefWidth(400);
            column.setMaxWidth(700);
            getColumns().add(column);
        });
    }

    @Autowired
    private void initActions(@Qualifier("beanTable") List<Function<BeanData, FxAction>> actions) {
        actions().addAll(actions);
    }

    @Autowired
    private void initTypeColumn(SignatureResolver signatureResolver,
                                BeanTypeResolver typeResolver,
                                ProjectProfile profile,
                                AppearanceSettings appearanceSettings) {
        typeColumn.setCellValueFactory(param -> Bindings.createStringBinding(
                () -> {
                    final BeanTypeInfo type = typeResolver.resolve(profile.getBeanContext(), param.getValue().getName());
                    return signatureResolver.postProcess(TypeToken.of(type.getType()).toString());
                },
                profile.getBeanFile().beans,
                appearanceSettings.showFullNamesProperty(),
                appearanceSettings.showGenericSignaturesProperty())
        );
    }

    @Autowired
    @Override
    public void installActions(SpecialActions specialActions) {
        super.installActions(specialActions);
    }
}
