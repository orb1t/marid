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
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.swing.input.InputControl;
import org.marid.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Configuration {

    class Pv<V, C extends InputControl<V>> implements PrefSupport {

        public final Class<?> caller = ClassResolver.CLASS_RESOLVER.getClassContext()[2];
        private Field field;
        private final Supplier<C> controlSupplier;
        private final Supplier<V> defaultValueSupplier;
        private final Preferences preferences;

        public Pv(Supplier<C> controlSupplier, Supplier<V> defaultValueSupplier) {
            this.controlSupplier = controlSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
            this.preferences = PrefUtils.preferences(caller, caller.isAnnotationPresent(Pref.class)
                    ? caller.getAnnotation(Pref.class).value()
                    : new String[]{StringUtils.decapitalize(caller.getSimpleName())});
        }

        @Override
        public Preferences preferences() {
            return preferences;
        }

        public V getDefaultValue() {
            return defaultValueSupplier.get();
        }

        public C getControl() {
            return controlSupplier.get();
        }

        public Input getInput() {
            return getField().getAnnotation(Input.class);
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

        public V get() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            return getPref(key, getDefaultValue(), input.tab());
        }

        public void save(C control) {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            putPref(key, control.getValue(), input.tab());
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

        private static final class ClassResolver extends SecurityManager {

            @Override
            protected Class[] getClassContext() {
                return super.getClassContext();
            }

            private static final ClassResolver CLASS_RESOLVER = new ClassResolver();
        }
    }
}
