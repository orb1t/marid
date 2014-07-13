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

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static org.marid.l10n.L10n.s;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractDialog extends JDialog implements WindowListener {

    protected final Logger log = Logger.getLogger(getClass().getName());

    public AbstractDialog(Window window, String title, ModalityType modalityType) {
        super(window, s(title), modalityType);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    protected final Action acceptAction = new MaridAction(getAcceptLabel(), getAcceptIcon(), e -> {
        try {
            accept();
        } catch (Exception x) {
            warning(log, "Accepting error", x);
        } finally {
            dispose();
        }
    });

    protected final Action rejectAction = new MaridAction(getRejectLabel(), getRejectIcon(), e -> {
        try {
            reject();
        } catch (Exception x) {
            warning(log, "Rejecting error", x);
        } finally {
            dispose();
        }
    });

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        rejectAction.actionPerformed(new ActionEvent(this, 0, "close"));
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    protected String getAcceptIcon() {
        return "ok";
    }

    protected String getRejectIcon() {
        return "cancel";
    }

    protected String getAcceptLabel() {
        return "OK";
    }

    protected String getRejectLabel() {
        return "Cancel";
    }

    protected void accept() {
    }

    protected void reject() {
    }

    protected abstract void fill(GroupLayout gl, SequentialGroup vg, SequentialGroup hg);

    protected void addDefaultButtons() {
        final JPanel panel = new JPanel();
        final GroupLayout gl = new GroupLayout(panel);
        gl.setAutoCreateGaps(true);
        final SequentialGroup vg = gl.createSequentialGroup();
        final ParallelGroup hg = gl.createParallelGroup();
        final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        vg.addComponent(separator);
        hg.addComponent(separator);
        final JButton acceptButton = new JButton(acceptAction);
        final JButton rejectButton = new JButton(rejectAction);
        vg.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(rejectButton)
                .addComponent(acceptButton));
        hg.addGroup(gl.createSequentialGroup()
                .addComponent(rejectButton)
                .addGap(32, 64, Integer.MAX_VALUE)
                .addComponent(acceptButton));
        gl.setVerticalGroup(vg);
        gl.setHorizontalGroup(hg);
        panel.setLayout(gl);
        add(panel, BorderLayout.SOUTH);
        rootPane.setDefaultButton(acceptButton);
    }

    @Override
    public void pack() {
        addWindowListener(this);
        final JPanel mainPanel = new JPanel();
        final GroupLayout gl = new GroupLayout(mainPanel);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        final SequentialGroup vg = gl.createSequentialGroup();
        final SequentialGroup hg = gl.createSequentialGroup();
        fill(gl, vg, hg);
        gl.setVerticalGroup(vg);
        gl.setHorizontalGroup(hg);
        mainPanel.setLayout(gl);
        add(mainPanel);
        rootPane.registerKeyboardAction(rejectAction, KeyStroke.getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
        super.pack();
        setLocationRelativeTo(getOwner());
    }
}
