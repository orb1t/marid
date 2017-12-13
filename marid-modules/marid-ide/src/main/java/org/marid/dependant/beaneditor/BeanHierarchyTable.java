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

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.marid.dependant.beaneditor.view.IdeBeanViewFactory;
import org.marid.ide.project.ProjectProfile;
import org.marid.idefx.beans.IdeBean;
import org.marid.idefx.beans.IdeBeanItem;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;

@Component
public class BeanHierarchyTable extends TreeTableView<IdeBean> {

  @Autowired
  public BeanHierarchyTable(@NotNull IdeBean root) {
    super(new IdeBeanItem(root));
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    getRoot().setExpanded(true);
  }

  @Order(0)
  @Bean(initMethod = "run")
  public Runnable nameColumn(IdeBeanViewFactory beanViewFactory) {
    return () -> {
      final TreeTableColumn<IdeBean, Node> nameColumn = new TreeTableColumn<>();
      nameColumn.textProperty().bind(ls("Name"));
      nameColumn.setCellValueFactory(p -> {
        final IdeBean bean = p.getValue().getValue();
        return createObjectBinding(() -> beanViewFactory.beanLabel(bean), bean.observables());
      });
      nameColumn.setPrefWidth(200);
      nameColumn.setMinWidth(50);
      nameColumn.setMaxWidth(1000);
      getColumns().add(nameColumn);
      setTreeColumn(nameColumn);
    };
  }

  @Order(1)
  @Bean(initMethod = "run")
  public Runnable typeColumn(ProjectProfile profile, IdeBeanViewFactory beanViewFactory) {
    return () -> {
      final TreeTableColumn<IdeBean, Node> column = new TreeTableColumn<>();
      column.textProperty().bind(ls("Type"));
      column.setCellValueFactory(p -> {
        final Observable[] observables = ArrayUtils.add(getRoot().getValue().observables(), profile);
        return createObjectBinding(() -> {
          final TreeItem<IdeBean> item = p.getValue();
          final IdeBean bean = item.getValue();
          if (bean == getRoot().getValue()) {
            return null;
          } else {
            return beanViewFactory.typeLabel(bean, profile);
          }
        }, observables);
      });
      column.setPrefWidth(200);
      column.setMinWidth(100);
      column.setMaxWidth(1000);
      getColumns().add(column);
    };
  }

  @Order(2)
  @Bean(initMethod = "run")
  public Runnable factoryColumn(IdeBeanViewFactory viewFactory) {
    return () -> {
      final TreeTableColumn<IdeBean, Node> column = new TreeTableColumn<>();
      column.textProperty().bind(ls("Factory"));
      column.setCellValueFactory(p -> {
        if (p.getValue() == getRoot()) {
          return null;
        } else {
          final IdeBean bean = p.getValue().getValue();
          return createObjectBinding(() -> viewFactory.createView(bean, bean.getFactory()), bean.factory);
        }
      });
      column.setPrefWidth(500);
      column.setMinWidth(300);
      column.setMaxWidth(3000);
      getColumns().add(column);
    };
  }

  @Autowired
  private void initActions(BeanActionProvider[] actionProviders, SpecialActions specialActions, ProjectProfile profile) {
    final ObjectBinding<ObservableList<FxAction>> actions = Bindings.createObjectBinding(() -> {
      final TreeItem<IdeBean> item = getSelectionModel().getSelectedItem();
      return Stream.of(actionProviders)
          .map(p -> p.apply(item))
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }, getSelectionModel().selectedItemProperty(), profile);
    specialActions.assign(focusedProperty(), actions);
    setRowFactory(param -> specialActions.wrap(new TreeTableRow<>(), actions::get));
  }
}
