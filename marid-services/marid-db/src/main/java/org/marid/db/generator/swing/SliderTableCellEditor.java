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

import org.marid.db.generator.swing.SwingNumericDaqGeneratorModel.TagInfo;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SliderTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
    private final JSlider slider = new JSlider(model);

    private ChangeListener changeListener;

    public SliderTableCellEditor() {
        slider.setRequestFocusEnabled(false);
        slider.setPaintTicks(true);
        slider.setPaintTrack(true);
        slider.setPaintLabels(true);
        slider.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
        slider.setFocusable(false);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final SwingNumericDaqGeneratorModel model = (SwingNumericDaqGeneratorModel) table.getModel();
        final TagInfo tagInfo = model.get(row);
        slider.setMinimum(tagInfo.minValue);
        slider.setMaximum(tagInfo.maxValue);
        slider.setMajorTickSpacing(Math.max((tagInfo.maxValue - tagInfo.minValue) / 10, 1));
        slider.setMinorTickSpacing(Math.max((tagInfo.maxValue - tagInfo.minValue) / 100, 1));
        slider.setValue(((Number) value).intValue());
        if (changeListener != null) {
            slider.removeChangeListener(changeListener);
        }
        slider.addChangeListener(changeListener = e -> {
            tagInfo.value = slider.getValue();
            model.fireTableCellUpdated(row, 4);
        });
        return slider;
    }

    @Override
    public Object getCellEditorValue() {
        return slider.getValue();
    }

    @Override
    public boolean stopCellEditing() {
        if (changeListener != null) {
            slider.removeChangeListener(changeListener);
        }
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        if (changeListener != null) {
            slider.removeChangeListener(changeListener);
        }
        super.cancelCellEditing();
    }
}
