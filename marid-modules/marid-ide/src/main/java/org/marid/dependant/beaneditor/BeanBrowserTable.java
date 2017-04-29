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

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.core.annotation.Order;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class BeanBrowserTable extends TableView<BeanDefinitionHolder> {

    final BeanMetaInfoProvider.BeansMetaInfo metaInfo;

    @Autowired
    public BeanBrowserTable(BeanMetaInfoProvider beanMetaInfoProvider) {
        metaInfo = beanMetaInfoProvider.metaInfo();
        getItems().addAll(metaInfo.beans());
        setEditable(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @Order(1)
    @Autowired
    public void nameColumn() {
        final TableColumn<BeanDefinitionHolder, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Name"));
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBeanName()));
        col.setPrefWidth(300);
        col.setMaxWidth(600);
        getColumns().add(col);
    }

    @Order(2)
    @Autowired
    public void typeColumn() {
        final TableColumn<BeanDefinitionHolder, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getBeanDefinition().getBeanClassName()));
        col.setPrefWidth(500);
        col.setMaxWidth(1000);
        getColumns().add(col);
    }

    @Order(3)
    @Autowired
    public void descriptionColumn() {
        final TableColumn<BeanDefinitionHolder, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Description"));
        col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getBeanDefinition().getDescription()));
        col.setPrefWidth(500);
        col.setMaxWidth(2000);
        getColumns().add(col);
    }
}
