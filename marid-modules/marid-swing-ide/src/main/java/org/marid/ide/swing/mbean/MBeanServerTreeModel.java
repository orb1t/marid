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

package org.marid.ide.swing.mbean;

import org.jdesktop.swingx.treetable.TreeTableModel;
import org.marid.ide.base.MBeanServerSupport;
import org.marid.ide.swing.mbean.node.Node;
import org.marid.ide.swing.mbean.node.RootNode;
import org.marid.l10n.L10nSupport;
import org.marid.swing.tree.TNode;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MBeanServerTreeModel implements TreeTableModel, L10nSupport {

    protected final MBeanServerSupport mBeanServerSupport;
    protected final EventListenerList listenerList = new EventListenerList();

    protected RootNode root;

    public MBeanServerTreeModel(MBeanServerSupport mBeanServerSupport) {
        this.mBeanServerSupport = mBeanServerSupport;
        this.root = mBeanServerSupport.serverResult(RootNode::new);
    }

    @Override
    public RootNode getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((TNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TNode) node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((TNode) parent).getIndex((TNode) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    public void update() {
        root = mBeanServerSupport.serverResult(RootNode::new);
        final TreeModelListener[] listeners = listenerList.getListeners(TreeModelListener.class);
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].treeStructureChanged(new TreeModelEvent(this, root == null ? null : new Object[]{getRoot()}));
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return TNode.class;
            case 2:
                return String.class;
            default:
                return Object.class;
        }
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return s("Name");
            case 1:
                return s("Value");
            case 2:
                return s("Description");
            default:
                return null;
        }
    }

    @Override
    public int getHierarchicalColumn() {
        return 0;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        switch (column) {
            case 0:
                return node;
            case 1:
                return null;
            case 2:
                return ((Node) node).getDescription();
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
    }
}
