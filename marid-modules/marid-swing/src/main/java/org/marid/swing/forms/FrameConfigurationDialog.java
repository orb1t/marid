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
import org.marid.swing.AbstractFrame;
import org.marid.swing.input.InputControl;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static javax.swing.JTabbedPane.TOP;
import static javax.swing.JTabbedPane.WRAP_TAB_LAYOUT;
import static org.marid.l10n.L10n.s;
import static org.marid.util.StringUtils.camelToText;
import static org.marid.util.StringUtils.capitalize;

/**
 * @author Dmitry Ovchinnikov
 */
public class FrameConfigurationDialog extends JDialog implements LogSupport, PrefSupport {

    private final JTabbedPane tabbedPane;

    public FrameConfigurationDialog(AbstractFrame owner) {
        super(owner, s("Configuration") + ": " + owner.getTitle(), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(tabbedPane = new JTabbedPane(getPref("tabPlacement", TOP), getPref("tabLayoutPolicy", WRAP_TAB_LAYOUT)));
        Arrays.<Tab>asList(owner.getClass().getAnnotationsByType(Tab.class))
                .stream()
                .sorted((t1, t2) -> t1.order() != t2.order() ? t1.order() - t2.order() : t1.node().compareTo(t2.node()))
                .forEachOrdered(tab -> {
                    try {
                        addTab(tab);
                    } catch (Exception x) {
                        warning("Unable to add {0}", tab.node());
                    }
                });
        setMinimumSize(new Dimension(700, 500));
        pack();
        setLocationRelativeTo(owner);
    }

    private void addTab(Tab tab) throws Exception {
        final String tabTitle = tab.label().isEmpty() ? s(capitalize(tab.node())) : s(tab.label());
        final Icon tabIcon = tab.icon().isEmpty() ? null : Images.getIcon(tab.icon(), 16);
        final String tabTip = tab.tip().isEmpty() ? null : s(tab.tip());
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setName(tab.node());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.BASELINE;
        c.insets = new Insets(5, 10, 5, 10);
        Arrays.asList(getOwner().getClass().getMethods())
                .stream()
                .filter(m -> m.isAnnotationPresent(Input.class) && m.getAnnotation(Input.class).tab().equals(tab.node()))
                .sorted((m1, m2) -> {
                    final Input a1 = m1.getAnnotation(Input.class);
                    final Input a2 = m2.getAnnotation(Input.class);
                    return a1.order() != a2.order()
                            ? a1.order() - a2.order()
                            : a1.name().compareTo(a2.name());
                })
                .forEachOrdered(method -> {
                    final Input input = method.getAnnotation(Input.class);
                    try {
                        final InputControl inputControl = (InputControl) method.invoke(getOwner());
                        inputControl.getComponent().setName(input.name().isEmpty() ? method.getName() : input.name());
                        addTabInputControl(panel, c, input, inputControl);
                    } catch (Exception x) {
                        warning("Unable to create input {0}.{1}", x, tab.node(), input.name());
                    }
                });
        c.weighty = 1.0;
        panel.add(Box.createVerticalBox(), c);
        tabbedPane.addTab(tabTitle, tabIcon, new JScrollPane(panel), tabTip);
    }

    private void addTabInputControl(JPanel p, GridBagConstraints c, Input input, InputControl ctl) throws Exception {
        c.gridwidth = input.vertical() ? GridBagConstraints.REMAINDER : GridBagConstraints.RELATIVE;
        c.weightx = input.vertical() ? 1.0 : 0.0;
        final String label = input.label().isEmpty() ? camelToText(ctl.getComponent().getName()) : input.label();
        p.add(new JLabel(s(label) + ": "), c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        p.add(ctl.getComponent(), c);
    }

    @Override
    public AbstractFrame getOwner() {
        return (AbstractFrame) super.getOwner();
    }
}
