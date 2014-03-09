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

package org.marid.swing.input;

import org.marid.l10n.L10n;

import javax.swing.*;
import java.net.InetSocketAddress;

/**
 * @author Dmitry Ovchinnikov
 */
public class InetSocketAddressInputControl extends JPanel implements InputControl<InetSocketAddress> {

    private final JTextField addressField = new JTextField();
    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));

    public InetSocketAddressInputControl() {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(true);
        final GroupLayout g = new GroupLayout(this);
        g.setAutoCreateGaps(true);
        final JLabel addressLabel = new JLabel(L10n.s("Address") + ":");
        final JLabel portLabel = new JLabel(L10n.s("Port") + ":");
        g.setVerticalGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(addressLabel).addComponent(addressField).addComponent(portLabel).addComponent(spinner));
        g.setHorizontalGroup(g.createSequentialGroup()
                .addComponent(addressLabel).addComponent(addressField).addComponent(portLabel).addComponent(spinner));
        setLayout(g);
    }

    @Override
    public InetSocketAddress getValue() {
        return InetSocketAddress.createUnresolved(addressField.getText(), (int) spinner.getValue());
    }

    @Override
    public void setValue(InetSocketAddress value) {
        addressField.setText(value.getHostString());
        spinner.setValue(value.getPort());
    }

    @Override
    public int getBaseline(int width, int height) {
        return addressField.getBaseline(width, height);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return addressField.getBaselineResizeBehavior();
    }
}
