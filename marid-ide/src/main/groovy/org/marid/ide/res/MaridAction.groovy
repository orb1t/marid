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

package org.marid.ide.res

import images.Images

import javax.swing.AbstractAction
import javax.swing.KeyStroke
import java.awt.event.ActionEvent

class MaridAction extends AbstractAction {

    private final Closure action;
    private final Closure enabledPredicate;

    /**
     * Constructs an action.
     * @param params Action parameters.
     */
    MaridAction(Map<String, Object> params) {
        action = params.action instanceof Closure ? (Closure)params.action : null;
        if (params.enabled instanceof Closure) {
            enabledPredicate = params.enabled as Closure;
        } else {
            if (params.enabled == false) enabled = false;
            enabledPredicate = null;
        }
        if (params.name != null) {
            putValue(NAME, params.name.toString().ls());
        }
        if (params.icon != null) {
            def size = params.get("size", 16) as int;
            def icon = Images.getIcon(params.icon.toString(), size, size);
            putValue(SMALL_ICON, icon);
        }
        if (params.description != null) {
            putValue(SHORT_DESCRIPTION, params.description.toString().ls());
        }
        if (params.shortcut != null) {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(params.shortcut.toString()));
        }
    }

    @Override
    void actionPerformed(ActionEvent e) {
        if (action != null) {
            action(e);
        }
    }

    @Override
    boolean isEnabled() {
        return enabledPredicate != null ? enabledPredicate.call() == true : super.isEnabled();
    }
}
