/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.swing;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class ComponentUtil1 {

    public static void main(String... args) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MyFrame frame = new MyFrame();
                frame.setVisible(true);
            }
        });
    }

    private static class MyFrame extends JFrame {

        public MyFrame() {
            super("A");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel p = new JPanel();
            add(p);
            JLabel label = new JLabel("B");
            p.add(label);
            label.addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    System.out.println("added");
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    System.out.println("removed");
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
            label.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    System.out.println("shown");
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    System.out.println("hidden");
                }
            });
            setPreferredSize(new Dimension(400, 300));
            pack();
        }
    }
}
