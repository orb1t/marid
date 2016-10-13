/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.db.generator.swing;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
class SwingNumericDaqGeneratorModel extends AbstractTableModel {

    private final ArrayList<TagInfo> list = new ArrayList<>();

    public TagInfo get(int index) {
        return list.get(index);
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    public void visitTagInfos(Consumer<TagInfo> consumer) {
        list.forEach(consumer);
    }

    public long maxTag() {
        return list.stream().mapToLong(t -> t.tag).max().orElse(0L);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            default:
                return Integer.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final TagInfo tagInfo = list.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return tagInfo.tag;
            case 1:
                return tagInfo.minValue;
            case 2:
                return tagInfo.maxValue;
            case 3:
                return tagInfo.value;
            case 4:
                return tagInfo.value;
            default:
                throw new IllegalArgumentException(Integer.toString(columnIndex));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 3:
                return false;
            default:
                return true;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return s("Tag");
            case 1:
                return s("Min");
            case 2:
                return s("Max");
            case 3:
                return s("Value");
            case 4:
                return s("Control");
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        final TagInfo tagInfo = list.get(rowIndex);
        switch (columnIndex) {
            case 0:
                tagInfo.tag = ((Number) aValue).longValue();
                break;
            case 1:
                tagInfo.minValue = ((Number) aValue).intValue();
                break;
            case 2:
                tagInfo.maxValue = ((Number) aValue).intValue();
                break;
            case 4:
                tagInfo.value = ((Number) aValue).intValue();
                break;
        }
    }

    public void add(TagInfo tagInfo) {
        list.add(tagInfo);
        fireTableRowsInserted(list.size() - 1, list.size() - 1);
    }

    public void sort() {
        list.sort(Comparator.comparingLong(i -> i.tag));
        fireTableDataChanged();
    }

    static class TagInfo {

        long tag = 1L;
        int minValue = 0;
        int maxValue = 100;
        int value = 0;
    }
}
