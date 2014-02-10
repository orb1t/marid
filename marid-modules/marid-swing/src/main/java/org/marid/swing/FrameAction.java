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
import org.marid.logging.LogSupport;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.beans.Introspector.decapitalize;
import static org.marid.l10n.L10n.s;
import static org.marid.util.StringUtils.capitalize;

/**
 * @author Dmitry Ovchinnikov
 */
@Target({ElementType.METHOD, ElementType.TYPE})
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

    abstract class FrameActionElement implements Comparable<FrameActionElement> {

        public final FrameAction frameAction;
        protected final String label;
        protected final String command;
        protected final Map<LogSupport, Action> actionMap = new IdentityHashMap<>();

        public FrameActionElement(String label, String command, FrameAction frameAction) {
            this.frameAction = frameAction;
            this.label = label;
            this.command = command;
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

        protected abstract Action getRawAction(final LogSupport caller);

        public Action getAction(final LogSupport caller) {
            if (actionMap.containsKey(caller)) {
                return actionMap.get(caller);
            }
            final Action action = getRawAction(caller);
            action.putValue(Action.ACTION_COMMAND_KEY, command);
            final String icon = frameAction.icon().isEmpty() ? command : frameAction.icon();
            final ImageIcon smallIcon = Images.getIcon(icon + "16.png", 16);
            if (smallIcon != null) {
                action.putValue(Action.SMALL_ICON, smallIcon);
            }
            final ImageIcon largeIcon = Images.getIcon(icon + "24.png", 24);
            if (largeIcon != null) {
                action.putValue(Action.LARGE_ICON_KEY, largeIcon);
            }
            if (!frameAction.info().isEmpty()) {
                action.putValue(Action.SHORT_DESCRIPTION, s(frameAction.info()));
            }
            if (!frameAction.key().isEmpty()) {
                action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(frameAction.key()));
            }
            actionMap.put(caller, action);
            return action;
        }
    }

    class MethodActionElement extends FrameActionElement {

        private final Method method;

        public MethodActionElement(Method method, FrameAction fa) {
            super(s(fa.label().isEmpty() ? capitalize(method.getName()) : fa.label()), method.getName(), fa);
            this.method = method;
        }

        @Override
        protected Action getRawAction(final LogSupport caller) {
            return new AbstractAction(s(label)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        method.invoke(caller, e, this);
                    } catch (Exception x) {
                        caller.warning("{0} error", x, method.getName());
                    }
                }
            };
        }
    }

    class InternalFrameActionElement extends FrameActionElement {

        private final Class<?> klass;

        public InternalFrameActionElement(Class<?> klass, FrameAction fa) {
            super(s(fa.label().isEmpty() ? klass.getSimpleName() : fa.label()), decapitalize(klass.getSimpleName()), fa);
            this.klass = klass;
        }

        @Override
        protected Action getRawAction(final LogSupport caller) {
            return new AbstractAction(s(label)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final Class<?> cc = caller.getClass();
                    try {
                        final Constructor<?> c = klass.getConstructor(cc, ActionEvent.class, Action.class);
                        final Object frame = c.newInstance(caller, e, this);
                        cc.getMethod("add", JInternalFrame.class).invoke(caller, frame);
                    } catch (Exception x) {
                        caller.warning("{0} error", x, klass);
                    }
                }
            };
        }
    }
}
