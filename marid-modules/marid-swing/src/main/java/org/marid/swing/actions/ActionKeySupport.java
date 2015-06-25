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

import org.marid.dyn.MetaInfo;
import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.marid.swing.actions.MaridAction.MaridActionListener;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ActionKeySupport extends LogSupport {

    default <T extends Action> T addAction(String key, T action) {
        return addAction(new ActionKey(key), action);
    }

    default MaridAction addAction(String key, String title, String icon, ActionListener listener, Object... args) {
        return addAction(key, new MaridAction(title, icon, listener, args));
    }

    default MaridAction addAction(String key, String title, String icon, MaridActionListener listener, Object... args) {
        return addAction(key, new MaridAction(title, icon, listener, args));
    }

    default <T extends Action> T addAction(ActionKey key, T action) {
        getActionMap().put(key, action);
        return action;
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

    default void fillActions() {
        for (final Field field : getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (!field.isAnnotationPresent(MetaInfo.class) || !Action.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                final Action action = (Action) field.get(this);
                final MetaInfo metaInfo = field.getAnnotation(MetaInfo.class);
                addAction(new ActionKey(metaInfo.path()), action);
            } catch (ReflectiveOperationException x) {
                log(WARNING, "Unable to get action from {0}", x, field);
            }
        }
    }
}
