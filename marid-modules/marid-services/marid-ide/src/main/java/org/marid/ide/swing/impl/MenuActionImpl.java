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

package org.marid.ide.swing.impl;

import images.Images;
import org.marid.ide.menu.MenuEntry;
import org.marid.l10n.Localized;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Dmitry Ovchinnikov
 */
class MenuActionImpl extends AbstractAction implements Localized {

    private final MenuEntry entry;

    public MenuActionImpl(MenuEntry entry) {
        this.entry = entry;
        putValue(ACTION_COMMAND_KEY, entry.getCommand());
        putValue(NAME, S.l(entry.getLabel()));
        final String shortcut = entry.getShortcut();
        final String icon = entry.getIcon();
        final String description = S.l(entry.getDescription());
        final String info = S.l(entry.getInfo());
        if (shortcut != null) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
        }
        if (icon != null) {
            final ImageIcon smallIcon = Images.getIcon(icon, 16, 16);
            if (smallIcon != null) {
                putValue(SMALL_ICON, smallIcon);
            }
        }
        if (description != null) {
            putValue(LONG_DESCRIPTION, description);
        }
        if (info != null) {
            putValue(SHORT_DESCRIPTION, info);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        entry.call(e);
    }

    public void update() {
        if (entry.isMutableDescription()) {
            putValue(LONG_DESCRIPTION, S.l(entry.getDescription()));
        }
        if (entry.isMutableInfo()) {
            putValue(SHORT_DESCRIPTION, S.l(entry.getInfo()));
        }
        if (entry.isMutableLabel()) {
            putValue(NAME, S.l(entry.getLabel()));
        }
        if (entry.isMutableIcon()) {
            putValue(SMALL_ICON, Images.getIcon(entry.getIcon(), 16, 16));
        }
        if (entry.hasSelectedPredicate()) {
            putValue(SELECTED_KEY, entry.isSelected());
        }
        if (entry.hasEnabledPredicate()) {
            setEnabled(entry.isEnabled());
        }
    }
}
