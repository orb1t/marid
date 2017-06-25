package org.marid.jfx.tree;

/*-
 * #%L
 * marid-fx
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

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface TreeUtils {

    static <T> Stream<TreeItem<T>> treeStream(TreeView<T> treeView) {
        return treeStream(treeView.getRoot());
    }

    static <T> Stream<TreeItem<T>> treeStream(TreeTableView<T> treeTableView) {
        return treeStream(treeTableView.getRoot());
    }

    static <T> Stream<TreeItem<T>> treeStream(TreeItem<T> treeItem) {
        return concat(of(treeItem), treeItem.getChildren().stream().flatMap(TreeUtils::treeStream));
    }
}
