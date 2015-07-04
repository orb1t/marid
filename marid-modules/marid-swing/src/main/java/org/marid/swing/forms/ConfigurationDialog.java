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

import images.Images;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.ComponentConfiguration;
import org.marid.swing.actions.MaridAction;
import org.marid.swing.geom.Dimensions;
import org.marid.swing.input.InputControl;
import org.marid.swing.input.TitledInputControl;
import org.marid.util.StringUtils;
import org.marid.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.prefs.Preferences;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.*;
import static java.lang.String.format;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JTabbedPane.WRAP_TAB_LAYOUT;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.*;
import static javax.swing.SwingConstants.VERTICAL;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConfigurationDialog extends JDialog implements LogSupport, PrefSupport, L10nSupport {

    protected final Preferences preferences;
    protected final JTabbedPane tabbedPane;
    private final Map<Component, ComponentHolder> containerMap = new IdentityHashMap<>();
    private final Map<String, String> keyLabelMap = new HashMap<>();

    public ConfigurationDialog(Window window, String title, ComponentConfiguration configuration) {
        super(window, title, ModalityType.MODELESS);
        this.preferences = configuration.preferences().node("$dialog");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(tabbedPane = new JTabbedPane(getPref("tabPlacement", TOP), getPref("tabLayoutPolicy", WRAP_TAB_LAYOUT)));
        fill(configuration);
        final JButton cclBtn = new JButton(new MaridAction("Cancel", "cancel.png", (a, e) -> dispose()));
        final JButton okBtn = new JButton(new MaridAction("OK", "ok.png", this::savePreferences));
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(new JButton(new MaridAction("Import preferences", "importPrefs", this::importPrefs)));
        buttonPanel.add(new JButton(new MaridAction("Export preferences", "exportPrefs", this::exportPrefs)));
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JButton(new MaridAction("Load defaults", "loadDefaults", this::loadDefaults)));
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
        if (savePreferences()) {
            dispose();
        }
    }

    protected boolean savePreferences() {
        final Map<ComponentHolder, Exception> exceptionMap = new IdentityHashMap<>();
        for (final Map.Entry<Component, ComponentHolder> e : containerMap.entrySet()) {
            final ComponentHolder ch = e.getValue();
            try {
                final Object oldValue = ch.initialValue;
                final Object newValue = ch.control.getInputValue();
                if (!Objects.equals(oldValue, newValue)) {
                    if (Objects.equals(ch.p.defaultValueSupplier.get(), newValue) && ch.p.isPresent()) {
                        ch.p.remove();
                        log(FINE, "Restored {0}", ch.p.key);
                    } else {
                        ch.save();
                        log(FINE, "Saved {0}: {1}", ch.p.key, newValue);
                    }
                }
            } catch (Exception x) {
                exceptionMap.put(e.getValue(), x);
                log(WARNING, "Unable to save {0}", x, e.getValue().p.key);
            }
        }
        if (exceptionMap.isEmpty()) {
            return true;
        } else {
            final JPanel outerPanel = new JPanel(new BorderLayout(0, 10));
            outerPanel.add(new JLabel(s("List of validation errors") + ":"), NORTH);
            final JPanel panel = new JPanel(new GridBagLayout());
            final GridBagConstraints c = new GridBagConstraints();
            c.fill = BOTH;
            c.anchor = BASELINE;
            c.insets = new Insets(5, 10, 5, 10);
            for (final Map.Entry<ComponentHolder, Exception> e : exceptionMap.entrySet()) {
                c.weightx = 0.0;
                c.gridwidth = RELATIVE;
                panel.add(new JLabel(format("<html><b>%s</b></html>", s("Tab") + ":")), c);
                c.weightx = 1.0;
                c.gridwidth = REMAINDER;
                panel.add(new JLabel(e.getKey().p.key), c);
                c.weightx = 0.0;
                c.gridwidth = RELATIVE;
                panel.add(new JLabel(format("<html><b>%s</b></html>", s("Label") + ":")), c);
                c.weightx = 1.0;
                c.gridwidth = REMAINDER;
                panel.add(new JLabel(keyLabelMap.get(e.getKey().p.key)), c);
                c.weightx = 0.0;
                c.gridwidth = RELATIVE;
                panel.add(new JLabel(format("<html><b>%s</b></html>", s("Message") + ": ")), c);
                c.weightx = 1.0;
                c.gridwidth = REMAINDER;
                panel.add(new JLabel(format("<html>%s</html>", e.getValue().getLocalizedMessage())), c);
                c.weightx = 1.0;
                c.gridwidth = REMAINDER;
                panel.add(new JSeparator(HORIZONTAL), c);
            }
            outerPanel.add(new JScrollPane(panel));
            outerPanel.add(new JLabel(m("Do you want to solve these errors?")), SOUTH);
            switch (JOptionPane.showConfirmDialog(this, outerPanel, s("Errors"), YES_NO_OPTION, WARNING_MESSAGE)) {
                case JOptionPane.NO_OPTION:
                    return true;
                default:
                    return false;
            }
        }
    }

    protected void loadDefaults(ActionEvent actionEvent) {
        containerMap.forEach((c, ch) -> ch.p.remove());
        containerMap.forEach((c, ch) -> ch.set(ch.p.defaultValueSupplier.get()));
    }

    protected void exportPrefs(ActionEvent actionEvent) {
    }

    protected void importPrefs(ActionEvent actionEvent) {

    }

    private void fill(ComponentConfiguration configuration) {
        final Map<String, JPanel> panelMap = new LinkedHashMap<>();
        final Map<String, GridBagConstraints> cmap = new HashMap<>();
        final Map<String, Tab> tabMap = new HashMap<>();
        for (final Tab tab : configuration.getClass().getAnnotationsByType(Tab.class)) {
            tabMap.put(tab.node(), tab);
        }
        for (final ComponentConfiguration.P<?> p : configuration.getPreferences()) {
            final String tab = p.tab == null ? "common" : p.tab;
            final JPanel panel = panelMap.computeIfAbsent(tab, n -> new JPanel(new GridBagLayout()));
            final GridBagConstraints c = cmap.computeIfAbsent(tab, n -> {
                final GridBagConstraints cs = new GridBagConstraints();
                cs.fill = BOTH;
                cs.anchor = GridBagConstraints.CENTER;
                cs.insets = new Insets(5, 5, 5, 5);
                return cs;
            });
            final ComponentHolder ch = new ComponentHolder(p);
            addTabCc(panel, c, ch);
            containerMap.put(ch.control.getComponent(), ch);
        }
        panelMap.forEach((k, v) -> {
            final GridBagConstraints c = cmap.get(k);
            c.weighty = 1.0;
            v.add(Box.createVerticalBox(), c);
            final Tab tab = tabMap.get(k);
            final String tabTitle = tab != null && !tab.label().isEmpty() ? s(tab.label()) : s(StringUtils.capitalize(k));
            final String tabTip = tab != null && !tab.tip().isEmpty() ? s(tab.tip()) : null;
            final ImageIcon tabIcon = tab != null ? Images.getIcon(tab.icon(), 16) : null;
            tabbedPane.addTab(tabTitle, tabIcon, new JScrollPane(v), tabTip);
        });
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

    private void addTabCc(JPanel p, GridBagConstraints c, ComponentHolder ch) {
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = NONE;
        p.add(new JButton(ch.getAction()), c);
        c.fill = BOTH;
        p.add(new JSeparator(VERTICAL), c);
        final String labelText = s(ch.p.name);
        if (ch.control instanceof TitledInputControl) {
            ((TitledInputControl) ch.control).setTitle(labelText + ":");
        } else {
            final JLabel label = new JLabel(labelText + ":");
            label.setLabelFor(ch.control.getComponent());
            p.add(label, c);
        }
        c.gridwidth = REMAINDER;
        c.weightx = 1.0;
        p.add(ch.control.getComponent(), c);
        keyLabelMap.put(ch.p.key, labelText);
    }

    private static class ComponentHolder {

        private final ComponentConfiguration.P<?> p;
        private final InputControl<?> control;
        private final Object initialValue;

        public ComponentHolder(ComponentConfiguration.P<?> p) {
            this.p = p;
            this.control = p.inputControlSupplier.get();
            this.initialValue = p.get();
            set(initialValue);
        }

        public MaridAction.MaridActionListener getActionListener() {
            return (a, e) -> set(p.defaultValueSupplier.get());
        }

        public MaridAction getAction() {
            return new MaridAction("", "defaultValue", getActionListener(), SHORT_DESCRIPTION, LS.s("Set default value"));
        }

        public void set(Object v) {
            Utils.<InputControl<Object>>cast(control).setInputValue(v);
        }

        public void save() {
            Utils.<ComponentConfiguration.P<Object>>cast(p).accept(control.getInputValue());
        }
    }
}
