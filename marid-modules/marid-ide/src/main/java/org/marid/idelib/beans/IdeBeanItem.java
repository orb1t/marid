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

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TreeItem;
import org.marid.jfx.track.Tracks;

import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toList;

public class IdeBeanItem extends TreeItem<IdeBean> {

  public IdeBeanItem(@Nonnull IdeBean bean) {
    super(bean);

    final ObservableList<TreeItem<IdeBean>> children = getChildren();

    children.setAll(bean.children.stream().map(IdeBeanItem::new).collect(toList()));

    Tracks.addListListener(this, bean.children, change -> {
      while (change.next()) {
        final int from = change.getFrom();
        final int to = change.getTo();
        if (change.wasUpdated()) {
          for (int i = from; i < to; i++) {
            final TreeItem<IdeBean> item = children.get(i);
            Event.fireEvent(item, new TreeModificationEvent<>(valueChangedEvent(), item));
          }
        } else if (change.wasRemoved()) {
          children.remove(from, to);
        } else if (change.wasAdded()) {
          children.addAll(from, change.getAddedSubList().stream().map(IdeBeanItem::new).collect(toList()));
        }
      }
    });
  }
}
