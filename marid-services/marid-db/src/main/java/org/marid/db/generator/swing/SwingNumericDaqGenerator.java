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

import org.marid.db.dao.NumericWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingNumericDaqGenerator {

    private final NumericWriter numericWriter;

    public SwingNumericDaqGenerator(NumericWriter numericWriter) {
        this.numericWriter = numericWriter;
    }

    @Autowired
    private void init(ConfigurableApplicationContext context) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        EventQueue.invokeLater(() -> {
            final SwingNumericDaqGeneratorFrame frame = new SwingNumericDaqGeneratorFrame(numericWriter);
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
}