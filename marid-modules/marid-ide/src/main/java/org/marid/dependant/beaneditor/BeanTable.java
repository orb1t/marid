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
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.DefaultStringConverter;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.marid.dependant.beaneditor.model.SignatureResolver;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.settings.AppearanceSettings;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.jfx.control.MaridTreeTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.misc.Builder.build;
import static org.marid.runtime.context.MaridRuntimeUtils.modifiers;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTable extends MaridTreeTableView<BeanData> {

    private final TreeTableColumn<BeanData, String> nameColumn;
    private final TreeTableColumn<BeanData, String> factoryColumn;
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
            column.setGraphic(glyphIcon("D_COMMENT_TEXT"));
            column.setCellValueFactory(param -> param.getValue().getValue().name);
            column.setCellFactory(param -> new TextFieldTreeTableCell<BeanData, String>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    final BeanData beanData = getTreeTableRow().getItem();
                    if (empty || beanData == null) {
                        setGraphic(null);
                    } else {
                        final HashCodeBuilder builder = new HashCodeBuilder();
                        builder.append(item);
                        for (BeanData d = beanData.parent; d != null; d = d.parent) {
                            builder.append(d.getName());
                        }
                        setGraphic(IdeShapes.circle(builder.hashCode(), 16));
                    }
                }
            });
            column.setEditable(true);
            getColumns().add(column);
        });

        factoryColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Factory"));
            column.setMinWidth(50);
            column.setPrefWidth(70);
            column.setMaxWidth(300);
            column.setEditable(false);
            column.setGraphic(glyphIcon("D_LINK"));
            column.setCellValueFactory(param -> param.getValue().getValue().factory);
            getColumns().add(column);
        });

        producerColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Producer"));
            column.setMinWidth(150);
            column.setPrefWidth(200);
            column.setMaxWidth(700);
            column.setGraphic(glyphIcon("D_FUNCTION"));
            column.setEditable(false);
            column.setCellValueFactory(param -> resolver.signature(param.getValue().valueProperty()));
            column.setCellFactory(param -> new TextFieldTreeTableCell<BeanData, String>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    final BeanData beanData = getTreeTableRow().getItem();
                    if (empty || beanData == null) {
                        setGraphic(null);
                    } else {
                        final int mods = modifiers(beanData.getSignature());
                        final HBox box = new HBox(4);
                        if (Modifier.isStatic(mods)) {
                            box.getChildren().add(glyphIcon("D_VIEW_MODULE"));
                        }
                        if (Modifier.isSynchronized(mods)) {
                            box.getChildren().add(glyphIcon("D_SYNC"));
                        }
                        if (Modifier.isFinal(mods)) {
                            box.getChildren().add(glyphIcon("F_HAND_STOP_ALT"));
                        }
                        if (Modifier.isAbstract(mods)) {
                            box.getChildren().add(glyphIcon("F_ADJUST"));
                        }
                        if (Modifier.isPublic(mods)) {
                            box.getChildren().add(glyphIcon("D_LOCK_OPEN"));
                        }
                        setGraphic(box);
                    }
                }
            });
            getColumns().add(column);
        });

        typeColumn = build(new TreeTableColumn<>(), column -> {
            column.textProperty().bind(ls("Type"));
            column.setMinWidth(100);
            column.setPrefWidth(400);
            column.setMaxWidth(700);
            column.setGraphic(glyphIcon("D_CAST_CONNECTED"));
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
                appearanceSettings.showFullNamesProperty()
        ));
    }
}
