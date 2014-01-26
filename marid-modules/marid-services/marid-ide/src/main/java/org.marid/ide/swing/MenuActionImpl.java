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

package org.marid.ide.swing;

import images.Images;
import org.marid.ide.menu.MenuEntry;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.marid.l10n.L10n.*;

/**
 * @author Dmitry Ovchinnikov
 */
class MenuActionImpl extends AbstractAction {

    private final MenuEntry entry;

    public MenuActionImpl(MenuEntry entry) {
        this.entry = entry;
        putValue(ACTION_COMMAND_KEY, entry.getCommand());
        putValue(NAME, s(entry.getLabel()));
        final String shortcut = entry.getShortcut();
        final String icon = entry.getIcon();
        final String description = s(entry.getDescription());
        final String info = s(entry.getInfo());
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
            putValue(LONG_DESCRIPTION, s(entry.getDescription()));
        }
        if (entry.isMutableInfo()) {
            putValue(SHORT_DESCRIPTION, s(entry.getInfo()));
        }
        if (entry.isMutableLabel()) {
            putValue(NAME, s(entry.getLabel()));
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
