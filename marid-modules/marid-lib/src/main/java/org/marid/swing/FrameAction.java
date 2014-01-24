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

package org.marid.swing;

import images.Images;
import org.marid.logging.Logged;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.marid.l10n.Localized.S;
import static org.marid.methods.LogMethods.warning;
import static org.marid.util.StringUtils.capitalize;

/**
 * @author Dmitry Ovchinnikov
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FrameAction {

    String key() default "";

    String label() default "";

    String icon() default "";

    String info() default "";

    boolean tool() default false;

    String group() default "";

    String path() default "";

    class FrameActionElement implements Comparable<FrameActionElement> {

        public final FrameAction frameAction;
        private final String label;
        private final Method method;
        private final Map<Logged, Action> actionMap = new IdentityHashMap<>();

        public FrameActionElement(Method method, FrameAction frameAction) {
            this.frameAction = frameAction;
            this.method = method;
            this.label = frameAction.label().isEmpty() ? capitalize(method.getName()) : frameAction.label();
        }

        @Override
        public int compareTo(FrameActionElement o) {
            final int pc = getPath().compareTo(o.getPath());
            if (pc != 0) {
                return pc;
            } else {
                final int gc = getGroup().compareTo(o.getGroup());
                if (gc != 0) {
                    return gc;
                } else {
                    return label.compareTo(o.label);
                }
            }
        }

        public String getLabel() {
            return label;
        }

        public String getPath() {
            return frameAction.path();
        }

        public String getGroup() {
            return frameAction.group();
        }

        public Action getAction(final Logged caller) {
            if (actionMap.containsKey(caller)) {
                return actionMap.get(caller);
            }
            final Action action = new AbstractAction(S.l(label)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        method.invoke(caller, e, this);
                    } catch (Exception x) {
                        warning(caller.getLogger(), "{0} error", x, method.getName());
                    }
                }
            };
            action.putValue(Action.ACTION_COMMAND_KEY, method.getName());
            final String icon = frameAction.icon().isEmpty() ? method.getName() : frameAction.icon();
            final ImageIcon smallIcon = Images.getIcon(icon + "16.png", 16);
            if (smallIcon != null) {
                action.putValue(Action.SMALL_ICON, smallIcon);
            }
            final ImageIcon largeIcon = Images.getIcon(icon + "24.png", 24);
            if (largeIcon != null) {
                action.putValue(Action.LARGE_ICON_KEY, largeIcon);
            }
            if (!frameAction.info().isEmpty()) {
                action.putValue(Action.SHORT_DESCRIPTION, S.l(frameAction.info()));
            }
            if (!frameAction.key().isEmpty()) {
                action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(frameAction.key()));
            }
            actionMap.put(caller, action);
            return action;
        }
    }
}
