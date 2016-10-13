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

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SliderTableCellRenderer implements TableCellRenderer {

    private final DefaultBoundedRangeModel model = new DefaultBoundedRangeModel();
    private final JSlider slider = new JSlider(model);

    public SliderTableCellRenderer() {
        slider.setRequestFocusEnabled(false);
        slider.setPaintTicks(true);
        slider.setPaintTrack(true);
        slider.setPaintLabels(true);
        slider.setFont(new Font(Font.DIALOG, Font.PLAIN, 8));
        slider.setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final SwingNumericDaqGeneratorModel model = (SwingNumericDaqGeneratorModel) table.getModel();
        final SwingNumericDaqGeneratorModel.TagInfo tagInfo = model.get(row);
        slider.setMinimum(tagInfo.minValue);
        slider.setMaximum(tagInfo.maxValue);
        slider.setMajorTickSpacing(Math.max((tagInfo.maxValue - tagInfo.minValue) / 10, 1));
        slider.setMinorTickSpacing(Math.max((tagInfo.maxValue - tagInfo.minValue) / 100, 1));
        slider.setValue(((Number) value).intValue());
        return slider;
    }
}
