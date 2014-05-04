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

package org.marid.swing.forms;

import org.marid.dyn.TypeCaster;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.swing.input.InputControl;
import org.marid.util.StringUtils;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Configuration {

    class Pv<V> implements PrefSupport, LogSupport {

        public final Class<?> caller = ClassResolver.CLASS_RESOLVER.getClassContext()[2];
        private Field field;
        private final Supplier<? extends InputControl<V>> controlSupplier;
        private final Supplier<V> defaultValueSupplier;
        private final Preferences preferences;
        private final Map<Object, List<BiConsumer<V, V>>> consumerMap = new WeakHashMap<>();

        public Pv(Supplier<? extends InputControl<V>> controlSupplier, Supplier<V> defaultValueSupplier) {
            this.controlSupplier = controlSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
            this.preferences = PrefUtils.preferences(caller, caller.isAnnotationPresent(Pref.class)
                    ? caller.getAnnotation(Pref.class).value()
                    : new String[]{StringUtils.decapitalize(caller.getSimpleName())});
        }

        @Override
        public Logger logger() {
            return LOGGERS.get(caller);
        }

        @Override
        public Preferences preferences() {
            return preferences;
        }

        public V getDefaultValue() {
            return defaultValueSupplier.get();
        }

        public InputControl<V> getControl() {
            return controlSupplier.get();
        }

        public Supplier<V> getDefaultValueSupplier() {
            return defaultValueSupplier;
        }

        private Field getField() {
            if (field == null) {
                for (final Field field : caller.getFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        final Input input = field.getAnnotation(Input.class);
                        if (input != null) {
                            try {
                                if (this == field.get(null)) {
                                    return this.field = field;
                                }
                            } catch (ReflectiveOperationException x) {
                                throw new IllegalStateException(x);
                            }
                        }
                    }
                }
                throw new IllegalStateException("No such field");
            } else {
                return field;
            }
        }

        private String getFieldName() {
            final Field field = getField();
            return field == null ? null : field.getName();
        }

        public V get() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            return getPref(key, getDefaultValue(), input.tab());
        }

        public void save(InputControl<V> control) {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            putPref(key, control.getInputValue(), input.tab());
        }

        public boolean contains() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            final Preferences p = preferences.node(input.tab());
            try {
                return asList(p.keys()).contains(key) || asList(p.childrenNames()).contains(key);
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public void remove() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            final Preferences p = preferences.node(input.tab());
            try {
                if (asList(p.keys()).contains(key)) {
                    p.remove(key);
                } else if (asList(p.childrenNames()).contains(key)) {
                    p.node(key).removeNode();
                }
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public <T> T get(Class<T> type) {
            return TypeCaster.TYPE_CASTER.cast(type, get());
        }

        public void addConsumer(JInternalFrame frame, BiConsumer<V, V> consumer) {
            frame.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameOpened(InternalFrameEvent e) {
                    info("Adding configuration listener to {0}", getFieldName());
                    appendConsumer(frame, consumer);
                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    info("Removing configuration listener from {0}", getFieldName());
                    removeConsumer(frame, consumer);
                    frame.removeInternalFrameListener(this);
                }
            });
        }

        public void addConsumer(Window window, BiConsumer<V, V> consumer) {
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    info("Adding configuration listener to {0}", getFieldName());
                    appendConsumer(window, consumer);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    info("Removing configuration listener from {0}", getFieldName());
                    appendConsumer(window, consumer);
                    window.removeWindowListener(this);
                }
            });
        }

        public void addConsumer(Component component, BiConsumer<V, V> consumer) {
            component.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if (e.getChanged() instanceof Window) {
                        addConsumer((Window) e.getChanged(), consumer);
                        component.removeHierarchyListener(this);
                    } else if (e.getChanged() instanceof JInternalFrame) {
                        addConsumer((JInternalFrame) e.getChanged(), consumer);
                        component.removeHierarchyListener(this);
                    }
                }
            });
        }

        private void appendConsumer(Object gcBase, BiConsumer<V, V> consumer) {
            consumerMap.computeIfAbsent(gcBase, o -> new LinkedList<>()).add(consumer);
        }

        private void removeConsumer(Object gcBase, BiConsumer<V, V> consumer) {
            final List<BiConsumer<V, V>> consumers = consumerMap.get(gcBase);
            if (consumers != null) {
                consumers.remove(consumer);
            }
        }

        public void clearConsumers(Object gcBase) {
            consumerMap.remove(gcBase);
        }

        public void fireConsumers(V oldValue, V newValue) {
            consumerMap.forEach((k, v) -> {
                for (final BiConsumer<V, V> consumer : v) {
                    consumer.accept(oldValue, newValue);
                }
            });
        }

        private static final class ClassResolver extends SecurityManager {

            @Override
            protected Class[] getClassContext() {
                return super.getClassContext();
            }

            private static final ClassResolver CLASS_RESOLVER = new ClassResolver();
        }
    }
}
