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

import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.geom.Dimensions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.JTabbedPane.WRAP_TAB_LAYOUT;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingConstants.TOP;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConfigurationDialog extends JDialog implements LogSupport, PrefSupport, L10nSupport {

    protected final Preferences preferences;
    protected final ConfigurationTabbedPane tabPane;

    public ConfigurationDialog(Window window, String title, ConfData configuration) {
        super(window, title, ModalityType.MODELESS);
        this.preferences = configuration.preferences().node("$dialog");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(tabPane = new ConfigurationTabbedPane(configuration, getPref("tabPlacement", TOP), getPref("tabLayoutPolicy", WRAP_TAB_LAYOUT)));
        final JButton cclBtn = new JButton(new MaridAction("Cancel", "cancel.png", (a, e) -> dispose()));
        final JButton okBtn = new JButton(new MaridAction("OK", "ok.png", this::savePreferences));
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(new JButton(new MaridAction("Import preferences", "importPrefs", tabPane::importPrefs)));
        buttonPanel.add(new JButton(new MaridAction("Export preferences", "exportPrefs", tabPane::exportPrefs)));
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JButton(new MaridAction("Load defaults", "loadDefaults", tabPane::loadDefaults)));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cclBtn);
        buttonPanel.add(okBtn);
        add(buttonPanel, SOUTH);
        setPreferredSize(getPref("size", Dimensions.atLeast(800, 600, getPreferredSize())));
        getRootPane().setDefaultButton(okBtn);
        getRootPane().registerKeyboardAction(cclBtn.getAction(), getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(window);
    }

    @Override
    public Preferences preferences() {
        return preferences;
    }

    protected void savePreferences(ActionEvent actionEvent) {
        if (tabPane.savePreferences()) {
            dispose();
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                putPref("size", getSize());
                break;
            case WindowEvent.WINDOW_OPENED:
                setSize(getPref("size", getSize()));
                break;
        }
        super.processWindowEvent(e);
    }
}
