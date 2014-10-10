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

package org.marid.swing.actions;

import org.marid.l10n.L10nSupport;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Dmitry Ovchinnikov.
 */
public class GenericAction extends AbstractAction implements L10nSupport {

    private final ActionListener actionListener;

    public GenericAction(String name, ActionListener actionListener) {
        super(LS.s(name));
        this.actionListener = actionListener;
    }

    public GenericAction(String name, ImageIcon icon, ActionListener actionListener) {
        super(LS.s(name), icon);
        this.actionListener = actionListener;
    }

    public GenericAction(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionListener.actionPerformed(e);
    }
}
