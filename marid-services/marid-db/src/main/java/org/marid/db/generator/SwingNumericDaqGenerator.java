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

package org.marid.db.generator;

import org.marid.db.dao.NumericWriter;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

import static java.awt.GridBagConstraints.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingNumericDaqGenerator implements LogSupport {

    private final NumericWriter numericWriter;

    public SwingNumericDaqGenerator(NumericWriter numericWriter) {
        this.numericWriter = numericWriter;
    }

    @Autowired
    private void init(ConfigurableApplicationContext context) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        EventQueue.invokeLater(() -> {
            final Frame frame = new Frame();
            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    final ContextClosedEvent e = (ContextClosedEvent) event;
                    if (e.getApplicationContext() == context) {
                        frame.dispose();
                    }
                }
            });
            frame.setVisible(true);
        });
    }

    private class Frame extends JFrame {

        private final GridBagLayout layout = new GridBagLayout();
        private final GridBagConstraints constraints = new GridBagConstraints(
                REMAINDER, RELATIVE, 1, 1, 1.0, 0.0, BASELINE, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        private final JPanel panel = new JPanel();

        private Frame() {
            super(s("Swing numeric DAQ generator"));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setJMenuBar(new JMenuBar());
            panel.setPreferredSize(new Dimension(800, 600));
            panel.setLayout(layout);
            add(new JScrollPane(panel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
            pack();
            setLocationRelativeTo(null);
        }
    }
}