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

package org.marid.bd.schema;

import images.Images;
import org.marid.bd.Block;
import org.marid.bd.BlockGroupProvider;
import org.marid.bd.BlockProvider;
import org.marid.pref.PrefSupport;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class BlockListWindow extends JDialog implements PrefSupport {

    protected final BlockList blockList = new BlockList();
    protected final Preferences preferences;

    public BlockListWindow(SchemaFrame schemaFrame) {
        super(schemaFrame, "Block list", false);
        preferences = schemaFrame.preferences();
        add(new JScrollPane(blockList));
        setPreferredSize(new Dimension(300, 500));
        pack();
        setLocation(getPref("blockListLocation", new Point(0, 0)));
        setSize(getPref("blockListSize", getPreferredSize()));
    }

    @Override
    public void dispose() {
        try {
            putPref("blockListLocation", getLocation());
            putPref("blockListSize", getSize());
        } finally {
            super.dispose();
        }
    }

    @Override
    public Preferences preferences() {
        return preferences;
    }

    protected static class Cr extends DefaultTreeCellRenderer {
        @Override
        public Cr getTreeCellRendererComponent(JTree t, Object v, boolean s, boolean e, boolean l, int r, boolean f) {
            final Cr cr = (Cr) super.getTreeCellRendererComponent(t, v, s, e, l, r, f);
            if (v instanceof Group) {
                final Group g = (Group) v;
                cr.setText(g.name);
                cr.setIcon(g.icon);
            } else if (v instanceof Block) {
                final Block block = (Block) v;
                cr.setText(block.getName());
                cr.setIcon(block.getVisualRepresentation());
            }
            return cr;
        }
    }

    protected static class BlockList extends JTree implements DndSource<Block> {

        public BlockList() {
            super(new BlockListModel());
            setTransferHandler(new MaridTransferHandler());
            setDragEnabled(true);
            setRootVisible(false);
            setCellRenderer(new Cr());
        }

        @Override
        public int getDndActions() {
            return DND_COPY;
        }

        @Override
        public BlockListModel getModel() {
            return (BlockListModel) super.getModel();
        }

        @Override
        public Block getDndObject() {
            if (getSelectionPath() == null) {
                return null;
            } else {
                if (getSelectionPath().getLastPathComponent() instanceof Block) {
                    return (Block) getSelectionPath().getLastPathComponent();
                } else {
                    return null;
                }
            }
        }
    }

    protected static abstract class Node<E> {

        protected final List<E> nodes = new ArrayList<>();

        public int indexOf(Object child) {
            return ((List<?>) nodes).indexOf(child);
        }
    }

    protected static class Root extends Node<Group> {

        public Root() {
            update();
        }

        public void update() {
            nodes.clear();
            final Map<String, Group> gmap = new TreeMap<>();
            BlockGroupProvider.visit(p -> p.visit((g, i) -> gmap.put(g, new Group(g, Images.getIcon(i)))));
            gmap.forEach((k, g) -> nodes.add(g));
            BlockProvider.visit(p -> p.visit((g, b) -> gmap.computeIfAbsent(g, k -> new Group(k, null)).nodes.add(b)));
        }
    }

    protected static class Group extends Node<Block> {

        protected final String name;
        protected final ImageIcon icon;

        public Group(String name, ImageIcon icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    protected static class BlockListModel implements TreeModel {

        protected final EventListenerList listenerList = new EventListenerList();
        protected final Root root = new Root();

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((Node) parent).nodes.get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((Node) parent).nodes.size();
        }

        @Override
        public boolean isLeaf(Object node) {
            return node instanceof Block || ((Node) node).nodes.isEmpty();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((Node) parent).indexOf(child);
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
            root.update();
            for (final TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
                listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
            }
        }
    }
}
