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
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.input.InputControl;
import org.marid.swing.input.TitledInputControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.prefs.Preferences;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.GridBagConstraints.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JTabbedPane.TOP;
import static javax.swing.JTabbedPane.WRAP_TAB_LAYOUT;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingConstants.HORIZONTAL;
import static javax.swing.SwingConstants.VERTICAL;
import static javax.swing.SwingUtilities.windowForComponent;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.swing.MaridAction.MaridActionListener;
import static org.marid.swing.forms.Configuration.Pv;
import static org.marid.swing.util.PanelUtils.groupedPanel;
import static org.marid.util.StringUtils.camelToText;
import static org.marid.util.StringUtils.capitalize;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConfigurationDialog<C extends Component & Configuration & PrefSupport> extends JDialog implements LogSupport, PrefSupport {

    private final C component;
    private final Map<Component, ComponentHolder<Object, InputControl<Object>>> containerMap = new IdentityHashMap<>();
    private final Map<String, String> tabLabelMap = new HashMap<>();
    private final Map<String, Map<String, String>> keyLabelMap = new HashMap<>();
    private final JTabbedPane tabbedPane;

    @SuppressWarnings("MagicConstant")
    public ConfigurationDialog(C component) {
        super(windowForComponent(component), s("Configuration") + ": " + s(component.getName()), APPLICATION_MODAL);
        this.component = component;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(tabbedPane = new JTabbedPane(getPref("tabPlacement", TOP), getPref("tabLayoutPolicy", WRAP_TAB_LAYOUT)));
        for (final Tab tab : tabs()) {
            try {
                addTab(tab);
            } catch (Exception x) {
                warning("Unable to add {0}", tab.node());
            }
        }
        final JButton impBtn = new JButton(new MaridAction("Import preferences", "importPrefs", (a, e) -> importPrefs()));
        final JButton expBtn = new JButton(new MaridAction("Export preferences", "exportPrefs", (a, e) -> exportPrefs()));
        final JButton defBtn = new JButton(new MaridAction("Load defaults", "loadDefaults", (a, e) -> loadDefaults()));
        final JButton cclBtn = new JButton(new MaridAction("Cancel", "cancel", (a, e) -> dispose()));
        final JButton okBtn = new JButton(new MaridAction("OK", "ok", (a, e) -> {
            if (savePreferences()) {
                dispose();
            }
        }));
        add(groupedPanel(GroupLayout::createSequentialGroup, GroupLayout::createParallelGroup, (g, h, v) -> {
            h.addComponent(impBtn).addComponent(expBtn).addGap(20).addComponent(defBtn);
            h.addGap(10, 10, Integer.MAX_VALUE);
            h.addComponent(cclBtn).addComponent(okBtn);
            v.addComponent(impBtn).addComponent(expBtn).addComponent(defBtn).addComponent(cclBtn).addComponent(okBtn);
        }), SOUTH);
        setPreferredSize(getPref("size", getPreferredSize()));
        getRootPane().setDefaultButton(okBtn);
        getRootPane().registerKeyboardAction(cclBtn.getAction(), getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(getOwner());
    }

    @Override
    public Preferences preferences() {
        return component.preferences().node("configurationDialogPreferences");
    }

    private Set<Tab> tabs() {
        final Set<Tab> tabs = new TreeSet<>((a, b) -> a.order() != b.order() ? a.order() - b.order() : a.node().compareTo(b.node()));
        for (final Class<?> i : component.getClass().getInterfaces()) {
            tabs.addAll(Arrays.asList(i.getAnnotationsByType(Tab.class)));
        }
        for (Class<?> c = component.getClass(); c != null; c = c.getSuperclass()) {
            tabs.addAll(Arrays.asList(c.getAnnotationsByType(Tab.class)));
        }
        return tabs;
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_CLOSED:
                putPref("size", getSize());
                break;
            case WindowEvent.WINDOW_OPENED:
                setSize(getPref("size", getSize()));
                break;
        }
    }

    protected boolean savePreferences() {
        final Map<ComponentHolder, Exception> exceptionMap = new IdentityHashMap<>();
        for (final Map.Entry<Component, ComponentHolder<Object, InputControl<Object>>> e : containerMap.entrySet()) {
            final ComponentHolder<Object, InputControl<Object>> ch = e.getValue();
            try {
                final Object oldValue = ch.initialValue;
                final Object newValue = ch.control.getValue();
                if (!Objects.equals(oldValue, newValue)) {
                    if (Objects.equals(ch.getDefaultValue(), newValue) && ch.pv.contains()) {
                        ch.pv.remove();
                        fine("Restored {0}.{1}", ch.node, ch.key);
                    } else {
                        ch.pv.save(ch.control);
                        fine("Saved {0}.{1}: {2}", ch.node, ch.key, newValue);
                    }
                    ch.pv.fireConsumers(oldValue, newValue);
                }
            } catch (Exception x) {
                exceptionMap.put(e.getValue(), x);
                warning("Unable to save {0}.{1}", x, e.getValue().node, e.getValue().key);
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
                panel.add(new JLabel(tabLabelMap.get(e.getKey().node)), c);
                c.weightx = 0.0;
                c.gridwidth = RELATIVE;
                panel.add(new JLabel(format("<html><b>%s</b></html>", s("Label") + ":")), c);
                c.weightx = 1.0;
                c.gridwidth = REMAINDER;
                panel.add(new JLabel(keyLabelMap.get(e.getKey().node).get(e.getKey().key)), c);
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

    protected void loadDefaults() {
        containerMap.forEach((c, ch) -> ch.pv.remove());
        containerMap.forEach((c, ch) -> ch.control.setValue(ch.getDefaultValue()));
    }

    protected void exportPrefs() {
    }

    protected void importPrefs() {

    }

    private void addTab(Tab tab) throws Exception {
        final String tabTitle = tab.label().isEmpty() ? s(capitalize(tab.node())) : s(tab.label());
        tabLabelMap.put(tab.node(), tabTitle);
        keyLabelMap.put(tab.node(), new HashMap<>());
        final Icon tabIcon = tab.icon().isEmpty() ? null : Images.getIcon(tab.icon(), 16);
        final String tabTip = tab.tip().isEmpty() ? null : s(tab.tip());
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5, 5, 5, 5);
        asList(component.getClass().getFields())
                .stream()
                .filter(f -> Modifier.isStatic(f.getModifiers()) &&
                        f.isAnnotationPresent(Input.class) &&
                        f.getAnnotation(Input.class).tab().equals(tab.node()))
                .sorted((f1, f2) -> {
                    final Input a1 = f1.getAnnotation(Input.class);
                    final Input a2 = f2.getAnnotation(Input.class);
                    return a1.order() != a2.order()
                            ? a1.order() - a2.order()
                            : a1.name().compareTo(a2.name());
                })
                .forEachOrdered(field -> {
                    final Input input = field.getAnnotation(Input.class);
                    try {
                        final ComponentHolder<Object, InputControl<Object>> ch = new ComponentHolder<>(input, field);
                        ch.control.setValue(ch.initialValue);
                        addTabCc(panel, c, input, ch);
                        containerMap.put(ch.control.getComponent(), ch);
                    } catch (Exception x) {
                        warning("Unable to create input {0}.{1}", x, tab.node(), input.name());
                    }
                });
        c.weighty = 1.0;
        panel.add(Box.createVerticalBox(), c);
        tabbedPane.addTab(tabTitle, tabIcon, new JScrollPane(panel), tabTip);
    }

    private void addTabCc(JPanel p, GridBagConstraints c, Input in, ComponentHolder<Object, ?> ch) throws Exception {
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = NONE;
        p.add(new JButton(ch.getAction()), c);
        c.fill = BOTH;
        p.add(new JSeparator(VERTICAL), c);
        final String labelText = s(in.label().isEmpty() ? camelToText(ch.key) : in.label());
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
        keyLabelMap.get(in.tab()).put(ch.key, labelText);
    }

    private static class ComponentHolder<V, C extends InputControl<V>> {

        private final String node;
        private final String key;
        private final C control;
        private final Pv<V, C> pv;
        private final V initialValue;

        @SuppressWarnings("unchecked")
        public ComponentHolder(Input input, Field field) {
            key = input.name().isEmpty() ? field.getName() : input.name();
            node = input.tab();
            try {
                pv = (Pv<V, C>) field.get(null);
                control = pv.getControl();
                initialValue = pv.get();
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }

        public V getDefaultValue() {
            return pv.getDefaultValue();
        }

        public MaridActionListener getActionListener() {
            return (a, e) -> control.setValue(getDefaultValue());
        }

        public MaridAction getAction() {
            return new MaridAction("", "defaultValue", getActionListener(), SHORT_DESCRIPTION, s("Set default value"));
        }
    }
}