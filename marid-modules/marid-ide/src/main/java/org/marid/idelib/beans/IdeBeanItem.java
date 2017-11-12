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

package org.marid.idelib.beans;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.WeakListChangeListener;
import javafx.event.Event;
import javafx.scene.control.TreeItem;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.dependant.beaneditor.view.IdeBeanView.glyph;

public class IdeBeanItem extends TreeItem<IdeBean> {

  public IdeBeanItem(@Nonnull IdeBean bean) {
    super(bean);
    getChildren().setAll(bean.children.stream().map(IdeBeanItem::new).collect(toList()));
    graphicProperty().bind(createObjectBinding(() -> glyph(bean.getFactory()), bean.factory));
    bean.children.addListener(new WeakListChangeListener<IdeBean>(this::onChildrenChange));
  }

  private void onChildrenChange(Change<? extends IdeBean> change) {
    while (change.next()) {
      final int from = change.getFrom();
      final int to = change.getTo();
      if (change.wasUpdated()) {
        for (int i = from; i < to; i++) {
          final TreeItem<IdeBean> item = getChildren().get(i);
          Event.fireEvent(item, new TreeModificationEvent<>(valueChangedEvent(), item));
        }
      } else if (change.wasRemoved()) {
        getChildren().remove(from, to);
      } else if (change.wasAdded()) {
        getChildren().addAll(from, change.getAddedSubList().stream().map(IdeBeanItem::new).collect(toList()));
      }
    }
  }
}
