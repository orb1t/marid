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

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ActionKeySupport {

    default void addAction(String key, Action action) {
        addAction(new ActionKey(key), action);
    }

    default void addAction(ActionKey key, Action action) {
        getActionMap().put(key, action);
    }

    default ActionMap getActionMap() {
        if (this instanceof RootPaneContainer) {
            return ((RootPaneContainer) this).getRootPane().getActionMap();
        } else {
            return null;
        }
    }

    default Action actionByKey(ActionKey actionKey) {
        final ActionMap actionMap = getActionMap();
        return actionMap != null ? actionMap.get(actionKey) : null;
    }

    default Action actionByKey(String actionKey) {
        return actionByKey(new ActionKey(actionKey));
    }
}
