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

import org.marid.Versioning;
import org.marid.dyn.TypeCaster;
import org.marid.functions.CachedSupplier;
import org.marid.image.MaridIcons;
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.swing.forms.Input;
import org.marid.swing.input.InputControl;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractFrame extends JFrame implements PrefSupport {

    protected final String version = Versioning.getImplementationVersion(getClass());

    public AbstractFrame(String title) {
        super(s(title));
        setIconImages(MaridIcons.ICONS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    public <T> T getPref(Class<T> type, String node, String key) {
        for (final Method method : getClass().getMethods()) {
            final Input input = method.getAnnotation(Input.class);
            if (input != null) {
                final ComponentMetaHolder meta = new ComponentMetaHolder(input, method);
                if (meta.node.equals(node) && meta.key.equals(key)) {
                    try {
                        final ControlContainer cc = (ControlContainer) method.invoke(this);
                        final Object value = PrefUtils.getPref(preferences().node("prefs").node(input.tab()), meta.key, cc.getDefaultValue());
                        return TypeCaster.TYPE_CASTER.cast(type, value);
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException("Unable to load value for " + node + "." + key);
                    }
                }
            }
        }
        throw new NoSuchElementException(node + "." + key);
    }

    public static <V, C extends InputControl<V>> ControlContainer<V, C> cc(Supplier<C> cs, Supplier<V> dvs) {
        return new ControlContainer<>(cs, dvs);
    }

    public static class ControlContainer<V, C extends InputControl<V>> {

        private final Supplier<C> controlSupplier;
        private final Supplier<V> defaultValueSupplier;

        private ControlContainer(Supplier<C> controlSupplier, Supplier<V> defaultValueSupplier) {
            this.controlSupplier = controlSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
        }

        public V getDefaultValue() {
            return defaultValueSupplier.get();
        }

        public C getControl() {
            return controlSupplier.get();
        }
    }

    public static class ComponentMetaHolder {

        public final String key;
        public final String node;

        public ComponentMetaHolder(Input input, Method method) {
            key = input.name().isEmpty() ? method.getName() : input.name();
            node = input.tab();
        }
    }

    public static class ComponentHolder<V, C extends InputControl<V>> extends ComponentMetaHolder {

        public final C control;
        public final V defaultValue;

        @SuppressWarnings("unchecked")
        public ComponentHolder(AbstractFrame owner, Input input, Method method) {
            super(input, method);
            try {
                final ControlContainer<V, C> cc = (ControlContainer<V, C>) method.invoke(owner);
                control = cc.getControl();
                defaultValue = cc.getDefaultValue();
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
    }
}
