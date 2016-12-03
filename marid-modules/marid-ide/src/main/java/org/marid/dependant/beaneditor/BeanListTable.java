/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.dependant.beaneditor;

import javafx.beans.binding.Bindings;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.beandata.BeanDataEditorConfiguration;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.panes.main.IdeToolbar;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanListTable extends MaridTableView<BeanData> {

    @Autowired
    public BeanListTable(BeanFile beanFile) {
        super(beanFile.beans);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
    }

    @OrderedInit(1)
    public void nameColumn(ProjectProfile profile) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> param.getValue().name);
        col.setCellFactory(param -> new TextFieldTableCell<BeanData, String>(new DefaultStringConverter()) {
            @Override
            public void commitEdit(String newValue) {
                final String oldValue = getItem();
                newValue = profile.generateBeanName(newValue);
                super.commitEdit(newValue);
                ProjectManager.onBeanNameChange(profile, oldValue, newValue);
            }
        });
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Type"));
        col.setCellValueFactory(param -> param.getValue().type);
        col.setPrefWidth(450);
        col.setMaxWidth(650);
        col.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        col.setEditable(true);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void factoryBeanColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory bean"));
        col.setCellValueFactory(param -> param.getValue().factoryBean);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        getColumns().add(col);
    }

    @OrderedInit(4)
    public void factoryMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory method"));
        col.setCellValueFactory(param -> param.getValue().factoryMethod);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        getColumns().add(col);
    }

    @Autowired
    public void initRowFactory(SpecialActions actions, ObjectProvider<BeanListActions> beanListActionsProvider) {
        setRowFactory(view -> {
            final TableRow<BeanData> row = new TableRow<>();
            final MaridContextMenu contextMenu = actions.contextMenu(Collections::emptyMap);
            contextMenu.addOnPreShow(menu -> {
                if (row.getItem() == null) {
                    return;
                }
                final BeanListActions beanListActions = beanListActionsProvider.getObject();
                final Type type = beanListActions.profile.getType(row.getItem()).orElse(null);
                if (type == null) {
                    return;
                }
                final Class<?> rawType = ResolvableType.forType(type).getRawClass();
                menu.getItems().add(new SeparatorMenuItem());
                menu.getItems().addAll(beanListActions.factoryItems(rawType, row.getItem()));
                menu.getItems().addAll(beanListActions.editors(rawType, row.getItem()));
            });
            row.setContextMenu(contextMenu);
            return row;
        });
    }

    @Autowired
    private void initEditAction(FxAction editAction, IdeDependants dependants) {
        editAction.on(this, action -> {
            action.setEventHandler(event -> dependants.start(
                    "beanDataEditor",
                    BeanDataEditorConfiguration.class,
                    c -> c.data = getSelectionModel().getSelectedItem()
            ));
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }

    @Autowired
    private void initAdd(FxAction addAction, ProjectProfile profile) {
        addAction.on(this, action -> {
            action.setEventHandler(event -> {
                final BeanData beanData = new BeanData();
                final String name = profile.generateBeanName("newBean");
                beanData.name.set(name);
                beanData.type.set(Object.class.getName());
                getItems().add(beanData);
            });
        });
    }

    @Autowired
    private void initDelete(FxAction removeAction) {
        removeAction.on(this, action -> {
            action.setEventHandler(event -> getItems().removeAll(getSelectionModel().getSelectedItems()));
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }

    @Autowired
    private void initClearAll(FxAction clearAllAction) {
        clearAllAction.on(this, action -> {
            action.setEventHandler(event -> getItems().clear());
            action.bindDisabled(Bindings.isEmpty(getItems()));
        });
    }

    @Autowired
    private void initToolbar(IdeToolbar toolbar, @Qualifier("beanList") Map<String, FxAction> actionMap) {
        toolbar.on(this, () -> actionMap);
    }
}
