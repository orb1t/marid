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

import org.marid.l10n.Localized;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static javax.swing.JComponent.*;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractDialog extends JDialog implements WindowListener, Localized {

    private boolean initialized;

    protected final Logger log = Logger.getLogger(getClass().getName());
    protected final Preferences prefs = getPreferences(getClass());

    public AbstractDialog(Window window, String title, ModalityType modalityType) {
        super(window, S.l(title), modalityType);
    }

    public AbstractDialog(Frame frame, String title, boolean modal) {
        super(frame, S.l(title), modal);
    }

    public AbstractDialog(Dialog dialog, String title, ModalityType modalityType) {
        super(dialog, S.l(title), modalityType);
    }

    protected final Action acceptAction = new MaridAction(getAcceptLabel(), getAcceptIcon()) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                accept();
            } catch (Exception x) {
                warning(log, "Accepting error", x);
            } finally {
                dispose();
            }
        }
    };

    protected final Action rejectAction = new MaridAction(getRejectLabel(), getRejectIcon()) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                reject();
            } catch (Exception x) {
                warning(log, "Rejecting error", x);
            } finally {
                dispose();
            }
        }
    };

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
        return "s16/ok.png";
    }

    protected String getRejectIcon() {
        return "s16/cancel.png";
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

    protected abstract void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg);

    protected void addDefaultButtons(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        vg.addGap(24, 32, Integer.MAX_VALUE);
        JButton acceptButton = new JButton(acceptAction);
        JButton rejectButton = new JButton(rejectAction);
        vg.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(rejectButton)
                .addComponent(acceptButton));
        hg.addGroup(gl.createSequentialGroup()
                .addComponent(rejectButton)
                .addGap(32, 64, Integer.MAX_VALUE)
                .addComponent(acceptButton));
        rootPane.setDefaultButton(acceptButton);
    }

    private void init() {
        addWindowListener(this);
        GroupLayout gl = new GroupLayout(getContentPane());
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        SequentialGroup vg = gl.createSequentialGroup();
        ParallelGroup hg = gl.createParallelGroup();
        fill(gl, vg, hg);
        gl.setVerticalGroup(vg);
        gl.setHorizontalGroup(hg);
        getContentPane().setLayout(gl);
        KeyStroke escape = KeyStroke.getKeyStroke("ESCAPE");
        rootPane.registerKeyboardAction(rejectAction, escape, WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(getOwner());
    }

    @Override
    public void setVisible(boolean b) {
        if (!initialized) {
            init();
            initialized = true;
        }
        super.setVisible(b);
    }
}
