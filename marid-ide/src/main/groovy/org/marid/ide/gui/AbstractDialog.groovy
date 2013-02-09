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
import org.marid.l10n.Localized

import java.awt.Dialog.ModalityType
import javax.swing.*
import javax.swing.GroupLayout.ParallelGroup
import javax.swing.GroupLayout.SequentialGroup
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.logging.Level

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
import static javax.swing.KeyStroke.getKeyStroke

@Log
@Mixin(WindowAdapter)
abstract class AbstractDialog extends JDialog {
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
    public AbstractDialog(Frame frame, String title, boolean modal, GraphicsConfiguration conf) {
        super(frame, Localized.S.l(title), modal, conf);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     * @param modal Modal flag.
     */
    public AbstractDialog(Frame frame, String title, boolean modal) {
        super(frame, Localized.S.l(title), modal);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     */
    public AbstractDialog(Frame frame, String title) {
        super(frame, Localized.S.l(title));
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     */
    public AbstractDialog(Frame frame) {
        super(frame);
        init();
    }

    /**
     * Default constructor.
     */
    public AbstractDialog() {
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Dialog title.
     * @param modality Modality.
     * @param conf Graphics configuration.
     */
    public AbstractDialog(Window w, String title, ModalityType modality, GraphicsConfiguration c) {
        super(w, Localized.S.l(title), modality, c);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Title.
     * @param modality Modality.
     */
    public AbstractDialog(Window window, String title, ModalityType modality) {
        super(window, Localized.S.l(title), modality);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Title.
     */
    public AbstractDialog(Window window, String title) {
        super(window, Localized.S.l(title));
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param dialog Dialog.
     * @param title Title.
     * @param modality Modality.
     * @param conf Graphics configuration.
     */
    public AbstractDialog(Dialog dialog, String title, ModalityType modality, GraphicsConfiguration conf) {
        super(dialog, Localized.S.l(title), modality, conf);
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
    protected void addDefaultButtons(
            GroupLayout gl,
            GroupLayout.SequentialGroup vg,
            GroupLayout.ParallelGroup hg) {
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
    }

    @Override
    void windowClosing(WindowEvent e) {
        reject();
    }
}
