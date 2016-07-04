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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10n;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTable extends TableView<BeanData> {

    @Autowired
    public BeanEditorTable(BeanFile beanFile, ProjectProfile profile) {
        super(beanFile.beans);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(true);
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Name"));
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
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Type"));
            col.setCellValueFactory(param -> param.getValue().type);
            col.setPrefWidth(450);
            col.setMaxWidth(650);
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Factory bean"));
            col.setCellValueFactory(param -> param.getValue().factoryBean);
            col.setPrefWidth(250);
            col.setMaxWidth(450);
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Factory method"));
            col.setCellValueFactory(param -> param.getValue().factoryMethod);
            col.setPrefWidth(250);
            col.setMaxWidth(450);
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Init trigger"));
            col.setCellValueFactory(param -> param.getValue().initMethod);
            col.setPrefWidth(200);
            col.setMaxWidth(350);
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Destroy trigger"));
            col.setCellValueFactory(param -> param.getValue().destroyMethod);
            col.setPrefWidth(200);
            col.setMaxWidth(350);
        }));
        getColumns().add(build(new TableColumn<BeanData, String>(), col -> {
            col.setText(L10n.s("Lazy"));
            col.setCellValueFactory(param -> param.getValue().lazyInit);
            col.setPrefWidth(60);
            col.setMaxWidth(100);
        }));
    }
}
