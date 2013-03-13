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

package org.marid.ide.swing.impl

import images.Images
import org.marid.ide.menu.MenuEntry

import javax.swing.AbstractAction
import javax.swing.KeyStroke
import java.awt.event.ActionEvent

/**
 * Menu action implementation.
 *
 * @author Dmitry Ovchinnikov 
 */
class MenuActionImpl extends AbstractAction {

    private final MenuEntry entry;

    MenuActionImpl(MenuEntry entry) {
        this.entry = entry;
        putValue(ACTION_COMMAND_KEY, entry.command);
        putValue(NAME, entry.label.ls());
        def shortcut = entry.shortcut;
        def icon = entry.icon;
        def description = entry.description?.ls();
        def info = entry.info?.ls();
        if (shortcut != null) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
        }
        if (icon != null) {
            def smallIcon = Images.getIcon(icon, 16, 16);
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
    void actionPerformed(ActionEvent e) {
        entry.call(e);
    }

    void update() {
        if (entry.mutableDescription) {
            putValue(LONG_DESCRIPTION, entry.description.ls());
        }
        if (entry.mutableInfo) {
            putValue(SHORT_DESCRIPTION, entry.info.ls());
        }
        if (entry.mutableLabel) {
            putValue(NAME, entry.label.ls());
        }
        if (entry.mutableIcon) {
            putValue(SMALL_ICON, Images.getIcon(entry.icon, 16, 16));
        }
        if (entry.hasSelectedPredicate()) {
            putValue(SELECTED_KEY, entry.isSelected());
        }
        if (entry.hasEnabledPredicate()) {
            enabled = entry.enabled;
        }
    }
}
