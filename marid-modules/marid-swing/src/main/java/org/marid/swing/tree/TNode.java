/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.tree;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface TNode<Q extends TNode<Q, P, C>, P extends TNode, C extends TNode<?, ? extends Q, ?>> extends TreeNode {

    P getParent();

    List<C> getChildren();

    default int getChildCount() {
        return getChildren().size();
    }

    default int getIndex(TreeNode node) {
        final List<? extends TreeNode> children = getChildren();
        return children.indexOf(node);
    }

    default boolean isLeaf() {
        return !getAllowsChildren() || getChildren().isEmpty();
    }

    default Enumeration children() {
        return Collections.enumeration(getChildren());
    }

    default TreeNode getChildAt(int childIndex) {
        return getChildren().get(childIndex);
    }

    default boolean getAllowsChildren() {
        return true;
    }

    default TNode<?, ?, ?> getRoot() {
        for (TNode<?, ?, ?> node = this; true; node = node.getParent()) {
            if (node.getParent() == null) {
                return node;
            }
        }
    }
}
