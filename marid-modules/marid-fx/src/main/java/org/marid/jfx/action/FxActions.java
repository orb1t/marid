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

import java.lang.reflect.Constructor;
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
        MethodLoop:
        for (final Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(Action.class)) {
                try {
                    final Action action = method.getAnnotation(Action.class);
                    final FxAction fxAction;
                    if (FxAction.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                        fxAction = (FxAction) method.invoke(this);
                    } else {
                        fxAction = new FxAction(action.tGroup(), action.group(), action.menu());
                        fxAction.setEventHandler(event -> {
                            try {
                                switch (method.getParameterCount()) {
                                    case 0:
                                        method.invoke(this);
                                        break;
                                    case 1:
                                        method.invoke(this, event);
                                        break;
                                    case 2:
                                        method.invoke(this, fxAction, event);
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Incorrect method signature");
                                }
                            } catch (Exception x) {
                                log(INFO, "Unable to execute {0}", x, method);
                            }
                        });
                    }
                    if (fxAction.getHint() == null && !action.hint().isEmpty()) {
                        fxAction.setHint(action.hint());
                    }
                    if (fxAction.getText() == null && !action.name().isEmpty()) {
                        fxAction.setText(action.name());
                    }
                    if (fxAction.getIcon() == null && !action.icon().isEmpty()) {
                        fxAction.setIcon(action.icon());
                    }
                    if (action.conf() != ActionConfigurer.class) {
                        for (final Constructor<?> constructor : action.conf().getConstructors()) {
                            final Class<?>[] argTypes = constructor.getParameterTypes();
                            if (argTypes.length == 1 && argTypes[0].isAssignableFrom(getClass())) {
                                final ActionConfigurer configurer = (ActionConfigurer) constructor.newInstance(this);
                                configurer.configure(fxAction);
                            }
                        }
                    }
                    actionMap.put(method.getName(), fxAction);
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
