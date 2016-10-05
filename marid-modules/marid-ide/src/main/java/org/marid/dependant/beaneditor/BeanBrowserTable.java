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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.stream.Collectors;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class BeanBrowserTable extends TableView<BeanBrowserTable.BeanBrowserItem> {

    @Autowired
    public BeanBrowserTable(BeanMetaInfoProvider beanMetaInfoProvider) {
        super(itemsFrom(beanMetaInfoProvider));
        setEditable(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<BeanBrowserItem, String> col = new TableColumn<>(s("Name"));
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().name));
        col.setPrefWidth(300);
        col.setMaxWidth(600);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<BeanBrowserItem, String> col = new TableColumn<>(s("Type"));
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().definition.getBeanClassName()));
        col.setPrefWidth(500);
        col.setMaxWidth(1000);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void descriptionColumn() {
        final TableColumn<BeanBrowserItem, String> col = new TableColumn<>(s("Description"));
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().definition.getDescription()));
        col.setPrefWidth(500);
        col.setMaxWidth(2000);
        getColumns().add(col);
    }

    private static ObservableList<BeanBrowserItem> itemsFrom(BeanMetaInfoProvider metaInfoProvider) {
        final BeanMetaInfoProvider.BeansMetaInfo metaInfo = metaInfoProvider.beans();
        return metaInfo.beans().stream()
                .map(e -> new BeanBrowserItem(e.getBeanName(), e.getBeanDefinition(), metaInfo))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static class BeanBrowserItem {

        public final String name;
        public final BeanDefinition definition;
        public final BeanMetaInfoProvider.BeansMetaInfo metaInfo;

        private BeanBrowserItem(String name, BeanDefinition definition, BeanMetaInfoProvider.BeansMetaInfo metaInfo) {
            this.name = name;
            this.definition = definition;
            this.metaInfo = metaInfo;
        }
    }
}
