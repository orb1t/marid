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

import javax.swing.table.TableColumn;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class MBeanServerTreeTable extends JXTreeTable {

    public MBeanServerTreeTable(MBeanServerSupport mBeanServerSupport) {
        super(new MBeanServerTreeModel(mBeanServerSupport));
        setRootVisible(false);
    }

    @Override
    protected void initializeColumnWidths() {
        final List<TableColumn> columns = getColumns(true);
        final int[] widths = {40, -1, 100};
        for (int i = 0; i < Math.min(widths.length, columns.size()); i++) {
            final TableColumn column = columns.get(i);
            final int width = widths[i];
            if (width > 0) {
                column.setMinWidth(width / 2);
                column.setPreferredWidth(width);
                column.setMaxWidth(width * 2);
            }
        }
    }

    @Override
    public MBeanServerTreeModel getTreeTableModel() {
        return (MBeanServerTreeModel) super.getTreeTableModel();
    }
}
