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

package org.marid.swing.forms;

import org.marid.image.MaridIcons;
import org.marid.swing.ListPanel;
import org.marid.swing.input.Input;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.marid.l10n.Localized.S;

/**
 * @author Dmitry Ovchinnikov
 */
public class InputForm extends JInternalFrame {

    protected final Input<?>[] inputs;

    @SafeVarargs
    public <T extends Component & Input<?>> InputForm(String name, String title, T... inputs) {
        super(S.l(title), true, false, true, true);
        setName(name);
        setFrameIcon(new ImageIcon(MaridIcons.ICONS.get(1)));
        this.inputs = inputs;
        final JPanel panel = new ListPanel();
        final GroupLayout g = new GroupLayout(panel);
        g.setAutoCreateGaps(true);
        g.setAutoCreateContainerGaps(true);
        final GroupLayout.SequentialGroup h = g.createSequentialGroup();
        final GroupLayout.SequentialGroup v = g.createSequentialGroup();
        final GroupLayout.ParallelGroup labelGroup = g.createParallelGroup();
        final GroupLayout.ParallelGroup inputGroup = g.createParallelGroup();
        h.addGroup(labelGroup);
        h.addGroup(inputGroup);
        for (final T input : inputs) {
            final JLabel label = new JLabel(S.l(input.getLabel()) + ":");
            v.addGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label).addComponent(input));
            labelGroup.addComponent(label);
            inputGroup.addComponent(input);
        }
        g.setVerticalGroup(v);
        g.setHorizontalGroup(h);
        panel.setLayout(g);
        add(new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        pack();
    }

    public Map<String, Object> getValueMap() {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final Input<?> input : inputs) {
            map.put(input.getName(), input.getValue());
        }
        return map;
    }

    public <T> T getValue(Class<T> type, String key) {
        for (final Input<?> input : inputs) {
            if (key.equals(type.getName())) {
                return type.cast(input.getValue());
            }
        }
        return null;
    }
}
