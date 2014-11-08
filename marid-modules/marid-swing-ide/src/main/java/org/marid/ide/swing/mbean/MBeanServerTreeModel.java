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

import org.marid.ide.base.MBeanServerSupport;

import javax.management.ObjectInstance;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MBeanServerTreeModel implements TreeModel {

    protected final MBeanServerSupport mBeanServerSupport;
    protected final EventListenerList listenerList = new EventListenerList();

    public MBeanServerTreeModel(MBeanServerSupport mBeanServerSupport) {
        this.mBeanServerSupport = mBeanServerSupport;
    }

    @Override
    public MBeanServerSupport getRoot() {
        return mBeanServerSupport;
    }

    protected List<?> getChildList(Object parent) {
        if (parent instanceof MBeanServerSupport) {
            final MBeanServerSupport support = (MBeanServerSupport) parent;
            final List<ObjectInstance> list = support.serverResult(s -> new ArrayList<>(s.queryMBeans(null, null)));
            return list == null ? Collections.emptyList() : list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getChildList(parent).get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return getChildList(parent).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getChildList(parent).indexOf(child);
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
        final TreeModelListener[] listeners = listenerList.getListeners(TreeModelListener.class);
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].treeStructureChanged(new TreeModelEvent(this, new Object[]{getRoot()}));
        }
    }
}
