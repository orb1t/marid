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

package org.marid.bd.components;

import org.marid.bd.Block;
import org.marid.l10n.L10n;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.GridBags;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.GridBagConstraints.BASELINE;
import static java.awt.GridBagConstraints.REMAINDER;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractBlockComponentEditor<B extends Block> extends JDialog implements PrefSupport, LogSupport {

    protected final B block;
    protected final JTabbedPane tabbedPane = new JTabbedPane();
    protected final Map<String, TabPane> tabPaneMap = new HashMap<>();

    public AbstractBlockComponentEditor(Window window, B block) {
        super(window, L10n.s("Settings") + ": " + block.getName(), ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.block = block;
        add(tabbedPane);
    }

    protected void afterInit() {
        tabPaneMap.values().forEach(TabPane::finish);
        final JPanel buttonPanel = new JPanel(new BorderLayout());
        final JButton okButton = new JButton(new MaridAction("Submit", "ok", this::submit));
        final JButton cancelButton = new JButton(new MaridAction("Cancel", "cancel", this::reject));
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.add(okButton, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(cancelButton.getAction(), getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
    }

    @Override
    public void dispose() {
        try {
            super.dispose();
        } finally {
            tabPaneMap.clear();
        }
    }

    protected TabPane tabPane(String tab) {
        return tabPaneMap.computeIfAbsent(tab, t -> {
            final TabPane tabPane = new TabPane(t);
            tabbedPane.addTab(L10n.s(t), new JScrollPane(tabPane));
            return tabPane;
        });
    }

    protected void submit(Action action, ActionEvent actionEvent) {
        try {
            onSubmit(action, actionEvent);
        } catch (Exception x) {
            warning("Submit error", x);
        } finally {
            dispose();
        }
    }

    protected void reject(Action action, ActionEvent actionEvent) {
        try {
            onReject(action, actionEvent);
        } catch (Exception x) {
            warning("Reject error", x);
        } finally {
            dispose();
        }
    }

    protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
    }

    protected void onReject(Action action, ActionEvent actionEvent) throws Exception {
    }

    public static class TabPane extends JPanel {

        protected final String tab;

        public TabPane(String tab) {
            super(new GridBagLayout());
            this.tab = tab;
        }

        public void addLine(String label, JComponent component) {
            add(new JLabel(L10n.s(label)), GridBags.constraints(1, 1, 0.0, 0.0, BASELINE));
            add(component, GridBags.constraints(REMAINDER, 1, 1.0, 0.0, BASELINE));
        }

        public void addSeparator() {
            add(new JSeparator(JSeparator.HORIZONTAL), GridBags.constraints(REMAINDER, 1, 1.0, 0.0, BASELINE));
        }

        public void finish() {
            add(Box.createGlue(), GridBags.constraints(REMAINDER, REMAINDER, 1.0, 1.0, BASELINE));
        }
    }
}
