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

import images.Images;
import org.marid.l10n.L10nSupport;
import org.marid.swing.actions.MaridAction;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridButtons implements L10nSupport {

    public static JButton browseButton(JTextComponent textField) {
        return new JButton(new MaridAction("Browse", "browse.png", (a, e) -> {
            JFileChooser fileChooser = new JFileChooser(textField.getText());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showDialog(textField, LS.s("Select"));
            if (result == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().toString());
            }
        }));
    }

    public static JToggleButton toggleButton(Action action, Consumer<JToggleButton> consumer) {
        final JToggleButton button = new JToggleButton(action);
        consumer.accept(button);
        return button;
    }

    public static JLabel resizeButton(boolean vertical, Component component) {
        final JLabel label = new JLabel(Images.getIcon(vertical ? "resizeV.png" : "resizeH.png"));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        return label;
    }
}
