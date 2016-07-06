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

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTable extends TableView<BeanData> {

    private final ProjectProfile profile;
    private final ProjectCacheManager projectCacheManager;

    @Autowired
    public BeanEditorTable(BeanFile beanFile, ProjectProfile profile, ProjectCacheManager projectCacheManager) {
        super(beanFile.beans);
        this.profile = profile;
        this.projectCacheManager = projectCacheManager;
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        getColumns().add(nameColumn());
        getColumns().add(typeColumn());
        getColumns().add(factoryBeanColumn());
        getColumns().add(factoryMethodColumn());
        getColumns().add(initMethodColumn());
        getColumns().add(destroyMethodColumn());
        getColumns().add(lazyColumn());
    }

    private TableColumn<BeanData, String> nameColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> param.getValue().name);
        col.setCellFactory(param -> new TextFieldTableCell<BeanData, String>(new DefaultStringConverter()) {
            @Override
            public void commitEdit(String newValue) {
                final String oldValue = getItem();
                super.commitEdit(newValue);
                ProjectManager.onBeanNameChange(profile, oldValue, newValue);
            }
        });
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        col.setEditable(true);
        return col;
    }

    private TableColumn<BeanData, String> typeColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Type"));
        col.setCellValueFactory(param -> param.getValue().type);
        col.setPrefWidth(450);
        col.setMaxWidth(650);
        return col;
    }

    private TableColumn<BeanData, String> factoryBeanColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory bean"));
        col.setCellValueFactory(param -> param.getValue().factoryBean);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        return col;
    }

    private TableColumn<BeanData, String> factoryMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Factory method"));
        col.setCellValueFactory(param -> param.getValue().factoryMethod);
        col.setPrefWidth(250);
        col.setMaxWidth(450);
        return col;
    }

    private TableColumn<BeanData, String> initMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Init method"));
        col.setCellValueFactory(param -> param.getValue().initMethod);
        col.setCellFactory(param -> {
            final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<>();
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        return col;
    }

    private TableColumn<BeanData, String> destroyMethodColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Destroy method"));
        col.setCellValueFactory(param -> param.getValue().destroyMethod);
        col.setCellFactory(param -> {
            final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<>();
            cell.setComboBoxEditable(true);
            return cell;
        });
        col.setPrefWidth(180);
        col.setMaxWidth(340);
        return col;
    }

    private TableColumn<BeanData, String> lazyColumn() {
        final TableColumn<BeanData, String> col = new TableColumn<>(s("Lazy"));
        col.setCellValueFactory(param -> param.getValue().lazyInit);
        col.setCellFactory(param -> {
            final ComboBoxTableCell<BeanData, String> cell = new ComboBoxTableCell<BeanData, String>("true", "false", "default", "null") {
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
        return col;
    }
}
