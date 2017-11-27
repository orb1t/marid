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

package org.marid.idelib.beans;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.WeakListChangeListener;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.marid.annotation.MetaInfo;
import org.marid.expression.generic.Expression;
import org.marid.jfx.icons.FontIcons;
import org.marid.types.Classes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.ofNullable;

public class IdeBeanItem extends TreeItem<IdeBean> {

  public IdeBeanItem(@Nonnull IdeBean bean) {
    super(bean);
    getChildren().setAll(bean.children.stream().map(IdeBeanItem::new).collect(toList()));
    graphicProperty().bind(Bindings.createObjectBinding(() -> icon(bean.getFactory()), bean.factory));
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

  @Nullable
  private static Node icon(@Nullable Expression expression) {
    if (expression == null) {
      return null;
    } else {
      return Classes.classes(expression.getClass())
          .flatMap(c -> ofNullable(c.getAnnotation(MetaInfo.class)))
          .map(i -> FontIcons.glyphIcon(i.icon()))
          .findFirst()
          .orElseThrow(IllegalStateException::new);
    }
  }
}
