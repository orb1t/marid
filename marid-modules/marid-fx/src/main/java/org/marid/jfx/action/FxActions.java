/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.jfx.action;

import org.marid.jfx.menu.MaridMenu;
import org.marid.jfx.toolbar.MaridToolbar;
import org.marid.logging.LogSupport;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
public class FxActions implements LogSupport {

    protected final Map<String, FxAction> actionMap = new LinkedHashMap<>();

    protected Map<String, FxAction> actionMap() {
        if (!actionMap.isEmpty()) {
            return actionMap;
        }
        for (final Method method : getClass().getMethods()) {
            if (FxAction.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                try {
                    final FxAction action = (FxAction) method.invoke(this);
                    actionMap.put(method.getName(), action);
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        if (actionMap.isEmpty()) {
            throw new IllegalStateException("No actions found");
        }
        return actionMap;
    }

    public MaridToolbar createToolbar() {
        final MaridToolbar toolbar = new MaridToolbar();
        toolbar.init(actionMap());
        return toolbar;
    }

    public MaridMenu createMenu() {
        final MaridMenu menu = new MaridMenu();
        menu.init(actionMap());
        return menu;
    }
}
