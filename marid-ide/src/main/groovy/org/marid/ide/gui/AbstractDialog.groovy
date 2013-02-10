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

package org.marid.ide.gui

import groovy.util.logging.Log
import org.marid.ide.res.MaridAction
import org.marid.l10n.Localized.S

import javax.swing.*
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.util.logging.Level

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import static javax.swing.KeyStroke.getKeyStroke

@Log
abstract class AbstractDialog extends JDialog implements WindowListener {
    /**
     * Accept action.
     */
    protected final def acceptAction = new MaridAction(acceptButtonName, null, acceptButtonIcon) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                accept();
            } catch (Exception x) {
                log.log(Level.WARNING, "Accepting error", x);
            } finally {
                dispose();
            }
        }
    };

    /**
     * Reject action.
     */
    protected final def rejectAction = new MaridAction(rejectButtonName, null, rejectButtonIcon) {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                reject();
            } catch (Exception x) {
                log.log(Level.WARNING, "Rejecting error", x);
            } finally {
                dispose();
            }
        }
    };

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     * @param modal Modal flag.
     * @param conf Graphics configuration.
     */
    AbstractDialog(Frame frame, String title, boolean modal, GraphicsConfiguration conf) {
        super(frame, S.l(title), modal, conf);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     * @param modal Modal flag.
     */
    AbstractDialog(Frame frame, String title, boolean modal) {
        super(frame, S.l(title), modal);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     */
    AbstractDialog(Frame frame, String title) {
        super(frame, S.l(title));
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     */
    AbstractDialog(Frame frame) {
        super(frame);
        init();
    }

    /**
     * Default constructor.
     */
    AbstractDialog() {
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param w Owner window.
     * @param title Dialog title.
     * @param modality Modality.
     * @param c Graphics configuration.
     */
    AbstractDialog(Window w, String title, Dialog.ModalityType modality, GraphicsConfiguration c) {
        super(w, S.l(title), modality, c);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Title.
     * @param modality Modality.
     */
    AbstractDialog(Window window, String title, Dialog.ModalityType modality) {
        super(window, S.l(title), modality);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Title.
     */
    AbstractDialog(Window window, String title) {
        super(window, S.l(title));
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param d Dialog.
     * @param title Title.
     * @param modality Modality.
     * @param c Graphics configuration.
     */
    AbstractDialog(Dialog d, String title, Dialog.ModalityType modality, GraphicsConfiguration c) {
        super(d, S.l(title), modality, c);
        init();
    }

    /**
     * Fills the contents.
     * @param gl Group layout.
     * @param vg Vertical group.
     * @param hg Horizontal group.
     */
    protected abstract void fill(GroupLayout gl, SequentialGroup vg, ParallelGroup hg);

    /**
     * Rejects the dialog.
     */
    protected void reject() {
    }

    /**
     * Accepts the dialog.
     */
    protected void accept() {
    }

    /**
     * Get an accept button label.
     * @return Accept button label.
     */
    protected String getAcceptButtonName() {
        return "OK";
    }

    /**
     * Get a reject button label.
     * @return Reject button label.
     */
    protected String getRejectButtonName() {
        return "Cancel";
    }

    /**
     * Get an accept button icon.
     * @return Accept button icon.
     */
    protected String getAcceptButtonIcon() {
        return "s16/ok.png";
    }

    /**
     * Get a reject button icon.
     * @return Reject button icon.
     */
    protected String getRejectButtonIcon() {
        return "s16/cancel.png";
    }

    /**
     * Adds default buttons.
     * @param gl Group layout.
     * @param vg Vertical group.
     * @param hg Horizontal group.
     */
    protected void addDefaultButtons(GroupLayout gl, SequentialGroup vg, ParallelGroup hg) {
        vg.addGap(24, 32, Integer.MAX_VALUE);
        JButton acceptButton = new JButton(acceptAction);
        JButton rejectButton = new JButton(rejectAction);
        vg.addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(rejectButton)
                .addComponent(acceptButton));
        hg.addGroup(gl.createSequentialGroup()
                .addComponent(rejectButton)
                .addGap(0, 0, Integer.MAX_VALUE)
                .addComponent(acceptButton));
        getRootPane().setDefaultButton(acceptButton);
    }

    private void init() {
        addWindowListener(this);
        GroupLayout gl = new GroupLayout(getContentPane());
        gl.autoCreateContainerGaps = true;
        gl.autoCreateGaps = true;
        GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
        GroupLayout.ParallelGroup hg = gl.createParallelGroup();
        fill(gl, vg, hg);
        gl.setVerticalGroup(vg);
        gl.setHorizontalGroup(hg);
        getContentPane().setLayout(gl);
        rootPane.registerKeyboardAction(
                rejectAction, getKeyStroke("ESCAPE"), WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    void windowClosing(WindowEvent e) {
        rejectAction.actionPerformed(new ActionEvent(this, 0, "close"));
    }

    @Override
    void windowOpened(WindowEvent e) {
    }

    @Override
    void windowClosed(WindowEvent e) {
    }

    @Override
    void windowIconified(WindowEvent e) {
    }

    @Override
    void windowDeiconified(WindowEvent e) {
    }

    @Override
    void windowActivated(WindowEvent e) {
    }

    @Override
    void windowDeactivated(WindowEvent e) {
    }
}
