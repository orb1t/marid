/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.ide;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.marid.image.MaridImage;
import org.marid.logging.Logging;

/**
 * Main IDE class.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MaridIde {

    /**
     * Entry point.
     *
     * @param args Command-line arguments.
     */
    public static void main(String... args) throws Exception {
        Logging.init(MaridIde.class, "log_ide.properties");
        BufferedImage img = MaridImage.getIcon(128);
        ImageIO.write(img, "PNG", new File("/home/dmitry/marid128.png"));
        JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new IdeFrame().setVisible(true);
            }
        });
    }
}
