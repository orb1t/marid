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
package org.marid.ide.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import javax.swing.GroupLayout;
import javax.swing.JDialog;

import javax.swing.GroupLayout.SequentialGroup;

/**
 * Abstract dialog.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractDialog extends JDialog {

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     * @param modal Modal flag.
     * @param conf Graphics configuration.
     */
    public AbstractDialog(Frame frame, String title, boolean modal,
            GraphicsConfiguration conf) {
        super(frame, title, modal, conf);
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
        super(frame, title, modal);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param frame Owner frame.
     * @param title Title.
     */
    public AbstractDialog(Frame frame, String title) {
        super(frame, title);
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
    public AbstractDialog(Window window, String title, ModalityType modality,
            GraphicsConfiguration conf) {
        super(window, title, modality, conf);
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
        super(window, title, modality);
        init();
    }

    /**
     * Constructs a dialog.
     *
     * @param window Owner window.
     * @param title Title.
     */
    public AbstractDialog(Window window, String title) {
        super(window, title);
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
    public AbstractDialog(Dialog dialog, String title, ModalityType modality,
            GraphicsConfiguration conf) {
        super(dialog, title, modality, conf);
        init();
    }

    /**
     * Fills the contents.
     * @param gl Group layout.
     * @param vg Vertical group.
     * @param hg Horizontal group.
     */
    protected abstract void fill(
            GroupLayout gl,
            SequentialGroup vg,
            SequentialGroup hg);
    
    /**
     * Fills the buttons section.
     * @param gl Group layout.
     * @param vg Vertical group.
     * @param hg Horizontal group.
     */
    protected abstract void fillButtons(
            GroupLayout gl,
            SequentialGroup vg,
            SequentialGroup hg);
    
    /**
     * Rejects the dialog.
     */
    protected void reject() {
        dispose();
    }
    
    /**
     * Accepts the dialog.
     */
    protected void accept() {
    }
    
    private void init() {
        GroupLayout gl = new GroupLayout(getContentPane());
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        SequentialGroup vg = gl.createSequentialGroup();
        SequentialGroup hg = gl.createSequentialGroup();
        fill(gl, vg, hg);
        getContentPane().setLayout(gl);
    }
}
