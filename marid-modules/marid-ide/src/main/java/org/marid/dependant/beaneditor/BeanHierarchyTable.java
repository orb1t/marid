/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.beaneditor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import org.marid.idelib.beans.IdeBean;
import org.marid.idelib.beans.IdeBeanItem;
import org.marid.jfx.control.MaridTreeTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.dependant.beaneditor.view.IdeBeanView.view;
import static org.marid.jfx.LocalizedStrings.ls;

@Component
public class BeanHierarchyTable extends MaridTreeTableView<IdeBean> {

  @Autowired
  public BeanHierarchyTable(@Nonnull IdeBean root) {
    super(new IdeBeanItem(root));
    setShowRoot(false);
  }

  @Order(0)
  @Bean(initMethod = "run")
  public Runnable nameColumn() {
    return () -> {
      final TreeTableColumn<IdeBean, String> nameColumn = new TreeTableColumn<>();
      nameColumn.textProperty().bind(ls("Name"));
      nameColumn.setCellValueFactory(p -> p.getValue().getValue().name);
      nameColumn.setPrefWidth(200);
      nameColumn.setMinWidth(50);
      nameColumn.setMaxWidth(1000);
      getColumns().add(nameColumn);
      setTreeColumn(nameColumn);
    };
  }

  @Order(1)
  @Bean(initMethod = "run")
  public Runnable typeColumn() {
    return () -> {
      final TreeTableColumn<IdeBean, String> column = new TreeTableColumn<>();
      column.textProperty().bind(ls("Type"));
      column.setCellValueFactory(p -> {
        final TreeItem<IdeBean> item = p.getValue();
        final IdeBean bean = item.getValue();
        if (bean.getParent() == null) {
          return new SimpleObjectProperty<>();
        } else {
          return new SimpleObjectProperty<>();
        }
      });
      column.setPrefWidth(200);
      column.setMinWidth(100);
      column.setMaxWidth(1000);
      getColumns().add(column);
    };
  }

  @Order(2)
  @Bean(initMethod = "run")
  public Runnable factoryColumn() {
    return () -> {
      final TreeTableColumn<IdeBean, Node> column = new TreeTableColumn<>();
      column.textProperty().bind(ls("Factory"));
      column.setCellValueFactory(p -> {
        final TreeItem<IdeBean> item = p.getValue();
        final IdeBean bean = item.getValue();
        if (bean.getParent() == null) {
          return new SimpleObjectProperty<>();
        } else {
          return createObjectBinding(() -> view(bean.getFactory()), bean.factory);
        }
      });
      column.setPrefWidth(500);
      column.setMinWidth(300);
      column.setMaxWidth(3000);
      getColumns().add(column);
    };
  }

  @Autowired
  private void initActions(List<BeanActionProvider> actionProviders) {
    actions().addAll(actionProviders);
  }
}
