/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.widgets.services;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServicesTable extends JTable {

    public ServicesTable(ClassLoader classLoader) {
        super(new ServicesTableModel(classLoader));
        setShowGrid(true);
        setGridColor(SystemColor.controlShadow);
        setRowHeight(24);
        setDefaultRenderer(Class.class, classTableCellRenderer());
    }

    protected void alignCols() {
        for (int c = 0; c < getColumnCount(); c++) {
            int width = 40;
            for (int r = 0; r < getRowCount(); r++) {
                final TableCellRenderer renderer = getCellRenderer(r, c);
                final Component component = prepareRenderer(renderer, r, c);
                width = Math.max(width, component.getPreferredSize().width);
            }
            getColumnModel().getColumn(c).setPreferredWidth(width);
        }
    }

    @Override
    public void validate() {
        super.validate();
        alignCols();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        alignCols();
    }

    private TableCellRenderer classTableCellRenderer() {
        final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        return (t, v, s, f, r, c) -> renderer.getTableCellRendererComponent(t, ((Class) v).getSimpleName(), s, f, r, c);
    }
}
