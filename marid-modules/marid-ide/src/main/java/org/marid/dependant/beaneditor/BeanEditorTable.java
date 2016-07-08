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

import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10n;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTable extends TableView<BeanData> {

    @Autowired
    public BeanEditorTable(BeanFile beanFile) {
        super(beanFile.beans);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
    }

    @Autowired
    @Order(1)
    private void nameColumn(ObjectFactory<BeanEditorActions> actions, ProjectProfile profile, ProjectCacheManager cacheManager) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> param.getValue().name);
        col.setCellFactory(param -> new TextFieldTableCell<BeanData, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    final BeanData beanData = getItems().get(getIndex());
                    setContextMenu(actions.getObject().contextMenu(beanData));
                }
            }

            @Override
            public void commitEdit(String newValue) {
                if (cacheManager.containsBean(profile, newValue)) {
                    final Alert alert = new Alert(Alert.AlertType.WARNING, L10n.m("Bean name already exists"), ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
                final String oldValue = getItem();
                super.commitEdit(newValue);
                ProjectManager.onBeanNameChange(profile, oldValue, newValue);
            }
        });
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        getColumns().add(col);
    }

    @Autowired
    @Order(2)
    private void typeColumn(ProjectProfile profile) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Type"));
        col.setCellValueFactory(param -> param.getValue().type);
        col.setPrefWidth(450);
        col.setMaxWidth(650);
        getColumns().add(col);
    }

    @Autowired
    @Order(3)
    private void factoryBeanColumn(ProjectProfile profile) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory bean"));
        col.setCellValueFactory(param -> param.getValue().factoryBean);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        getColumns().add(col);
    }

    @Autowired
    @Order(4)
    private void factoryMethodColumn(ProjectProfile profile) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory method"));
        col.setCellValueFactory(param -> param.getValue().factoryMethod);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        getColumns().add(col);
    }

    private TableCell<BeanData, String> methodCell(TableColumn<BeanData, String> column,
                                                   ProjectProfile profile,
                                                   ProjectCacheManager cacheManager) {
        final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<BeanData, String>() {
            @Override
            public void startEdit() {
                final BeanData beanData = BeanEditorTable.this.getItems().get(getIndex());
                getItems().clear();
                final Class<?> type = cacheManager.getBeanClass(profile, beanData).orElse(null);
                if (type != null) {
                    getItems().addAll(Stream.of(type.getMethods())
                            .filter(method -> method.getParameterCount() == 0)
                            .filter(method -> method.getReturnType() == void.class)
                            .filter(method -> method.getDeclaringClass() != Object.class)
                            .filter(method -> !method.isAnnotationPresent(Autowired.class))
                            .filter(method -> !method.isAnnotationPresent(PostConstruct.class))
                            .filter(method -> !method.isAnnotationPresent(PreDestroy.class))
                            .filter(method -> !"close".equals(method.getName()))
                            .filter(method -> !"destroy".equals(method.getName()))
                            .map(Method::getName)
                            .collect(Collectors.toList()));
                }
                super.startEdit();
            }
        };
        cell.setComboBoxEditable(true);
        return cell;
    }

    @Autowired
    @Order(5)
    private void initMethodColumn(ProjectProfile profile, ProjectCacheManager cacheManager) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Init method"));
        col.setCellValueFactory(param -> param.getValue().initMethod);
        col.setCellFactory(param -> methodCell(param, profile, cacheManager));
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        getColumns().add(col);
    }

    @Autowired
    @Order(6)
    private void destroyMethodColumn(ProjectProfile profile, ProjectCacheManager cacheManager) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Destroy method"));
        col.setCellValueFactory(param -> param.getValue().destroyMethod);
        col.setCellFactory(param -> methodCell(param, profile, cacheManager));
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        getColumns().add(col);
    }

    @Autowired
    @Order(7)
    private void lazyColumn(ProjectProfile profile) {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Lazy"));
        col.setCellValueFactory(param -> param.getValue().lazyInit);
        col.setCellFactory(param -> {
            final String[] items = {"true", "false", "default", "null"};
            final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<BeanData, String>(items) {
                @Override
                public void commitEdit(String newValue) {
                    if ("null".equals(newValue)) {
                        newValue = null;
                    }
                    super.commitEdit(newValue);
                }
            };
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(100);
        col.setMaxWidth(150);
        col.setEditable(true);
        getColumns().add(col);
    }
}
