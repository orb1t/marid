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

import javafx.beans.InvalidationListener;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.project.ProjectProfileReflection;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.data.AbstractData;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.providers.BeanDataProvider;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanListTable extends MaridTableView<BeanData> {

    private final ProjectProfileReflection reflection;
    private final Map<BeanData, InvalidationListener> invalidationListenerMap = new HashMap<>();

    @Autowired
    public BeanListTable(BeanDataProvider beanDataProvider, ProjectProfileReflection reflection) {
        super(beanDataProvider.beanData());
        this.reflection = reflection;
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

    private TableCell<BeanData, String> methodCell(TableColumn<BeanData, String> column) {
        final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<BeanData, String>() {
            @Override
            public void startEdit() {
                final BeanData beanData = BeanListTable.this.getItems().get(getIndex());
                getItems().clear();
                final Class<?> type = reflection.getClass(beanData).orElse(null);
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

    @OrderedInit(5)
    public void initMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Init method"));
        col.setCellValueFactory(param -> param.getValue().initMethod);
        col.setCellFactory(this::methodCell);
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        getColumns().add(col);
    }

    @OrderedInit(6)
    public void destroyMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Destroy method"));
        col.setCellValueFactory(param -> param.getValue().destroyMethod);
        col.setCellFactory(this::methodCell);
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        getColumns().add(col);
    }

    @OrderedInit(7)
    public void lazyColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Lazy"));
        col.setCellValueFactory(param -> param.getValue().lazyInit);
        col.setCellFactory(param -> {
            final String[] items = {"true", "false", "default"};
            final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<>(items);
            final ContextMenu contextMenu = new ContextMenu();
            {
                final MenuItem item = new MenuItem(s("Clear value"), FontIcons.glyphIcon(FontIcon.M_CLEAR));
                item.setOnAction(event -> {
                    final BeanData beanData = getItems().get(cell.getIndex());
                    beanData.lazyInit.set(null);
                });
                contextMenu.getItems().add(item);
            }
            cell.setContextMenu(contextMenu);
            return cell;
        });
        col.setPrefWidth(100);
        col.setMaxWidth(150);
        col.setEditable(true);
        getColumns().add(col);
    }

    @Autowired
    public void initRowFactory(ObjectFactory<BeanListActions> actions) {
        setRowFactory(view -> {
            final TableRow<BeanData> row = new TableRow<>();
            row.itemProperty().addListener((o, ov, nv) -> {
                if (nv == null) {
                    row.setContextMenu(null);
                    invalidationListenerMap.computeIfPresent(ov, (v, old) -> {
                        v.removeListener(old);
                        return null;
                    });
                } else {
                    final InvalidationListener l = observable -> row.setContextMenu(actions.getObject().contextMenu(nv));
                    l.invalidated(nv);
                    nv.addListener(l);
                    invalidationListenerMap.compute(nv, (v, old) -> {
                        if (old != null) {
                            v.removeListener(old);
                        }
                        return l;
                    });
                }
            });
            return row;
        });
    }

    @PreDestroy
    public void destroy() {
        invalidationListenerMap.forEach(AbstractData::removeListener);
    }
}
