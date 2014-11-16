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

import org.jdesktop.swingx.JXTreeTable;
import org.marid.ide.base.MBeanServerSupport;
import org.marid.ide.swing.mbean.node.AttributeNode;
import org.marid.ide.swing.mbean.node.BeanNode;
import org.marid.jmx.JmxAttribute;
import org.marid.pref.PrefSupport;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.jmx.SwingJmxAttribute;

import javax.swing.table.TableColumn;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MBeanServerTreeTable extends JXTreeTable implements PrefSupport, DndSource<SwingJmxAttribute> {

    public MBeanServerTreeTable(MBeanServerSupport mBeanServerSupport) {
        super(new MBeanServerTreeModel(mBeanServerSupport));
        setRootVisible(false);
        setTreeCellRenderer(new MBeanTreeCellRenderer());
        setDragEnabled(true);
        setTransferHandler(new MaridTransferHandler());
    }

    @Override
    protected void initializeColumnWidths() {
        final List<TableColumn> columns = getColumns(true);
        columns.get(0).setMaxWidth(1000);
        columns.get(1).setMaxWidth(1000);
        columns.get(0).setPreferredWidth(getPref("nameWidth", 450));
        columns.get(1).setPreferredWidth(getPref("valueWidth", 600));
    }

    @Override
    public MBeanServerTreeModel getTreeTableModel() {
        return (MBeanServerTreeModel) super.getTreeTableModel();
    }

    public void savePreferences() {
        putPref("nameWidth", getColumn(0).getWidth());
        putPref("descriptionWidth", getColumn(1).getWidth());
    }

    public void update() {
        getTreeTableModel().update();
    }

    @Override
    public int getDndActions() {
        return DND_LINK;
    }

    @Override
    public SwingJmxAttribute getDndObject() {
        final int row = getSelectedRow();
        if (row < 0) {
            return null;
        }
        final Object object = getModel().getValueAt(row, getTreeTableModel().getHierarchicalColumn());
        if (object instanceof AttributeNode) {
            final AttributeNode node = (AttributeNode) object;
            final BeanNode beanNode = node.getParent().getParent();
            return new SwingJmxAttribute(new JmxAttribute(beanNode.getObjectName(), node.getName()));
        } else {
            return null;
        }
    }
}
