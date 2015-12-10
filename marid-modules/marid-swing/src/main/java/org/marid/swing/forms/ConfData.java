/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import org.marid.dyn.MetaInfo;
import org.marid.pref.PrefSupport;
import org.marid.swing.input.InputControl;
import org.marid.swing.pref.SwingPrefCodecs;
import org.marid.util.StringUtils;
import org.marid.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ConfData implements PrefSupport {

    public JDialog configurationDialog(Window window, String title) {
        return new ConfigurationDialog(window, title, this);
    }

    private P<?> pFromField(Field field, ConfData cc) {
        try {
            final P<?> p = (P<?>) field.get(cc);
            final MetaInfo metaInfo = field.getAnnotation(MetaInfo.class);
            if (metaInfo != null) {
                if (p.tab == null && !metaInfo.group().isEmpty()) {
                    p.tab = metaInfo.group();
                }
                if (p.icon == null && !metaInfo.icon().isEmpty()) {
                    p.icon = metaInfo.icon();
                }
                if (p.description == null && !metaInfo.description().isEmpty()) {
                    p.description = metaInfo.description();
                }
                if (p.name == null && !metaInfo.name().isEmpty()) {
                    p.name = metaInfo.name();
                }
            }
            if (p.name == null) {
                p.name = StringUtils.capitalize(field.getName());
            }
            return p;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    private List<P<?>> getPreferences(ConfData cc) {
        return Arrays.stream(cc.getClass().getFields())
                .filter(f -> f.getType() == P.class)
                .sorted((f1, f2) -> {
                    final MetaInfo m1 = f1.getAnnotation(MetaInfo.class), m2 = f2.getAnnotation(MetaInfo.class);
                    final int o1 = m1 != null ? m1.order() : 0, o2 = m2 != null ? m2.order() : 0;
                    final int c1 = Integer.compare(o1, o2);
                    if (c1 != 0) {
                        return c1;
                    } else {
                        final String n1 = m1 != null && !m1.name().isEmpty() ? m1.name() : f1.getName();
                        final String n2 = m2 != null && !m2.name().isEmpty() ? m2.name() : f2.getName();
                        return n1.compareTo(n2);
                    }
                })
                .map(field -> pFromField(field, cc))
                .collect(Collectors.toList());
    }

    public List<P<?>> getPreferences() {
        return getPreferences(this);
    }

    protected <V> P<V> p(Supplier<? extends InputControl<? extends V>> ics, Supplier<V> dvs) {
        return new P<>(ics, dvs);
    }

    public final class P<V> implements Supplier<V>, Consumer<V> {

        public final Supplier<Class<V>> type;
        public final Supplier<String> key;
        public final Supplier<? extends InputControl<V>> inputControlSupplier;
        public final Supplier<V> defaultValueSupplier;

        public String tab;
        public String description;
        public String icon;
        public String name;

        private P(Supplier<? extends InputControl<? extends V>> ics, Supplier<V> dvs) {
            this.type = () -> {
                for (final Field field : ConfData.this.getClass().getFields()) {
                    if (field.getType() == P.class) {
                        try {
                            final Object o = field.get(ConfData.this);
                            if (o == this) {
                                final ParameterizedType type = (ParameterizedType) field.getGenericType();
                                final Type t = type.getActualTypeArguments()[0];
                                return t instanceof ParameterizedType
                                        ? Utils.cast(((ParameterizedType) t).getRawType())
                                        : Utils.cast(t);
                            }
                        } catch (ReflectiveOperationException x) {
                            throw new IllegalStateException(x);
                        }
                    }
                }
                throw new IllegalStateException("No such field");
            };
            this.key = () -> {
                for (final Field field : ConfData.this.getClass().getFields()) {
                    if (field.getType() == P.class) {
                        try {
                            final Object o = field.get(ConfData.this);
                            if (o == this) {
                                return field.getName();
                            }
                        } catch (ReflectiveOperationException x) {
                            throw new IllegalStateException(x);
                        }
                    }
                }
                throw new IllegalStateException("No such field");
            };
            this.inputControlSupplier = Utils.cast(ics);
            this.defaultValueSupplier = dvs;
        }

        @Override
        public void accept(V v) {
            putPref(type.get(), key.get(), v);
        }

        @Override
        public V get() {
            return getPref(type.get(), key.get(), defaultValueSupplier.get());
        }

        public boolean isPresent() {
            try {
                return Arrays.asList(preferences().keys()).contains(key);
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public void remove() {
            preferences().remove(key.get());
        }

        public P<V> addListener(JInternalFrame frame, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(frame, type.get(), preferences(), key.get(), consumer);
            return this;
        }

        public P<V> addListener(Window window, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(window, type.get(), preferences(), key.get(), consumer);
            return this;
        }

        public P<V> addListener(Component component, Consumer<V> consumer) {
            SwingPrefCodecs.addConsumer(component, type.get(), preferences(), key.get(), consumer);
            return this;
        }

        public P<V> tab(String tab) {
            this.tab = tab;
            return this;
        }

        public P<V> description(String description) {
            this.description = description;
            return this;
        }

        public P<V> icon(String icon) {
            this.icon = icon;
            return this;
        }

        public P<V> name(String name) {
            this.name = name;
            return this;
        }
    }
}
