/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.dependant.beaneditor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import org.marid.beans.IdeBean;
import org.marid.beans.IdeBeanItem;
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
