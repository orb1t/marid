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
import org.marid.image.MaridIcons;
import org.marid.pref.PrefSupport;
import org.marid.pref.PrefUtils;
import org.marid.swing.forms.Input;
import org.marid.swing.input.InputControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.Arrays.asList;
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
        setLocation(getPref("location", new Point(0, 0)));
        setPreferredSize(getPref("size", new Dimension(700, 500)));
        setState(getPref("state", getState()));
        setExtendedState(getPref("extendedState", getExtendedState()));
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                setState(getPref("state", getState()));
                setExtendedState(getPref("extendedState", getExtendedState()));
                break;
            case WindowEvent.WINDOW_CLOSED:
                if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                    putPref("size", getSize());
                    putPref("location", getLocation());
                }
                putPref("state", getState());
                putPref("extendedState", getExtendedState());
                break;
        }
    }

    public class Pv<V, C extends InputControl<V>> {

        private Field field;
        private final Supplier<C> controlSupplier;
        private final Supplier<V> defaultValueSupplier;

        public Pv(Supplier<C> controlSupplier, Supplier<V> defaultValueSupplier) {
            this.controlSupplier = controlSupplier;
            this.defaultValueSupplier = defaultValueSupplier;
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
                for (final Field field : AbstractFrame.this.getClass().getFields()) {
                    final Input input = field.getAnnotation(Input.class);
                    if (input != null) {
                        try {
                            if (this == field.get(AbstractFrame.this)) {
                                return this.field = field;
                            }
                        } catch (ReflectiveOperationException x) {
                            throw new IllegalStateException(x);
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
            return PrefUtils.getPref(preferences().node("prefs").node(input.tab()), key, getDefaultValue());
        }

        public void save(C control) {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            PrefUtils.putPref(preferences().node("prefs").node(input.tab()), key, control.getValue());
        }

        public boolean contains() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            final Preferences preferences = preferences().node("prefs").node(input.tab());
            try {
                return asList(preferences.keys()).contains(key) || asList(preferences.childrenNames()).contains(key);
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public void remove() {
            final Field field = getField();
            final Input input = field.getAnnotation(Input.class);
            final String key = input.name().isEmpty() ? field.getName() : input.name();
            final Preferences preferences = preferences().node("prefs").node(input.tab());
            try {
                if (asList(preferences.keys()).contains(key)) {
                    preferences.remove(key);
                } else if (asList(preferences.childrenNames()).contains(key)) {
                    preferences.node(key).removeNode();
                }
            } catch (BackingStoreException x) {
                throw new IllegalStateException(x);
            }
        }

        public <T> T get(Class<T> type) {
            return TypeCaster.TYPE_CASTER.cast(type, get());
        }
    }
}
