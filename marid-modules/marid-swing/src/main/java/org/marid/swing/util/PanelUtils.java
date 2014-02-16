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

package org.marid.swing.util;

import javax.swing.*;
import java.util.function.Function;

import javax.swing.GroupLayout.Group;

/**
 * @author Dmitry Ovchinnikov
 */
public class PanelUtils {

    public static JPanel groupedPanel(Function<GroupLayout, Group> h, Function<GroupLayout, Group> v, GroupedAction a) {
        final JPanel panel = new JPanel();
        final GroupLayout g = new GroupLayout(panel);
        g.setAutoCreateGaps(true);
        g.setAutoCreateContainerGaps(true);
        final Group hGroup = h.apply(g);
        final Group vGroup = v.apply(g);
        a.doWithGroups(g, hGroup, vGroup);
        g.setHorizontalGroup(hGroup);
        g.setVerticalGroup(vGroup);
        panel.setLayout(g);
        return panel;
    }

    public static interface GroupedAction {

        void doWithGroups(GroupLayout g, Group h, Group v);
    }
}
