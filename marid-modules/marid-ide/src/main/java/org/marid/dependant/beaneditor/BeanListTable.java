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

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;
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
    public void initRowFactory(ObjectFactory<BeanListActions> actions) {
        setRowFactory(view -> new TableRow<BeanData>() {
            @Override
            protected void updateItem(BeanData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setContextMenu(null);
                } else {
                    setContextMenu(new ContextMenu() {
                        @Override
                        public void show(Node anchor, double screenX, double screenY) {
                            getItems().clear();
                            getItems().addAll(actions.getObject().contextMenu(item));
                            super.show(anchor, screenX, screenY);
                        }
                    });
                }
            }
        });
    }

    @Autowired
    private void initEditAction(SpecialActions specialActions, ObjectFactory<BeanListActions> actions) {
        specialActions.setEditAction(this, ls("Edit..."), event -> actions.getObject().onEdit(event));
    }
}
