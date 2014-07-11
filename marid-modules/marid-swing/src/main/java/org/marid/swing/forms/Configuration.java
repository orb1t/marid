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
import org.marid.swing.pref.SwingPrefCodecs;
import org.marid.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.marid.functions.Functions.safePredicate;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Configuration {

    class Pv<V> implements PrefSupport, LogSupport {

        private static final Map<Pv, PvData> PV_MAP = Collections.synchronizedMap(new WeakHashMap<>());

        public final Class<?> caller = ClassResolver.CLASS_RESOLVER.getClassContext()[2];
        private final Supplier<? extends InputControl<V>> controlSupplier;
        private final Supplier<V> defaultValueSupplier;
        private final Preferences preferences;

        public Pv(Supplier<? extends InputControl<V>> controlSupplier, Supplier<V> defaultValueSupplier) {
            this.controlSupplier = controlSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
            this.preferences = PrefUtils.preferences(caller, inferNodes());
        }

        private String[] inferNodes() {
            final Pref pref = caller.getAnnotation(Pref.class);
            return pref != null ? pref.value() : new String[]{StringUtils.decapitalize(caller.getSimpleName())};
        }

        @Override
        public Logger logger() {
            return LOGGERS.get(caller);
        }

        @Override
        public Preferences preferences() {
            return preferences;
        }

        public InputControl<V> getControl() {
            return controlSupplier.get();
        }

        public Supplier<V> getDefaultValueSupplier() {
            return defaultValueSupplier;
        }

        @SuppressWarnings("unchecked")
        public PvData<V> getPvData() {
            return PV_MAP.computeIfAbsent(this, pv -> stream(pv.caller.getFields())
                    .filter(safePredicate(f -> isStatic(f.getModifiers())
                            && f.isAnnotationPresent(Input.class)
                            && f.get(null) == pv))
                    .map(PvData::new)
                    .findFirst()
                    .get());
        }

        public V get() {
            return getPref(getPvData().type, getPvData().getKey(), getDefaultValueSupplier().get(), getPvData().getTab());
        }

        public void save(InputControl<V> control) {
            putPref(getPvData().type, getPvData().getKey(), control.getInputValue(), getPvData().getTab());
        }

        public boolean contains() {
            final Preferences p = preferences.node(getPvData().getTab());
            try {
                return asList(p.keys()).contains(getPvData().getKey());
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public void remove() {
            final Preferences p = preferences.node(getPvData().getTab());
            try {
                if (asList(p.keys()).contains(getPvData().getKey())) {
                    p.remove(getPvData().getKey());
                }
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public <T> T get(Class<T> type) {
            return TypeCaster.TYPE_CASTER.cast(type, get());
        }

        public void addConsumer(JInternalFrame frame, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(frame, getPvData().type, preferences().node(getPvData().getTab()), getPvData().getKey(), consumer);
        }

        public void addConsumer(Window window, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(window, getPvData().type, preferences().node(getPvData().getTab()), getPvData().getKey(), consumer);
        }

        public void addConsumer(Component component, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(component, getPvData().type, preferences().node(getPvData().getTab()), getPvData().getKey(), consumer);
        }

        public static final class PvData<V> {

            public final Field field;
            public final Input input;
            public final Class<V> type;

            @SuppressWarnings("unchecked")
            private PvData(Field field) {
                this.field = field;
                this.input = field.getAnnotation(Input.class);
                this.type = (Class<V>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            }

            public String getKey() {
                return input.name().isEmpty() ? field.getName() : input.name();
            }

            public String getTab() {
                return input.tab();
            }
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
