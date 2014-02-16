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
import org.marid.image.MaridIcons;
import org.marid.pref.PrefSupport;
import org.marid.swing.input.InputControl;

import javax.swing.*;

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

    public static <V, C extends InputControl<V>> ControlContainer<V, C> ccs(C ctrl, Supplier<V> dvs) {
        return new ControlContainer<>(ctrl, dvs);
    }

    public static <V, C extends InputControl<V>> ControlContainer<V, C> ccv(C ctrl, V defaultValue) {
        return new ControlContainer<>(ctrl, () -> defaultValue);
    }

    public static class ControlContainer<V, C extends InputControl<V>> {

        public final C control;
        private final Supplier<V> defaultValueSupplier;

        private ControlContainer(C control, Supplier<V> defaultValueSupplier) {
            this.control = control;
            this.defaultValueSupplier = defaultValueSupplier;
        }

        public V getDefaultValue() {
            return defaultValueSupplier.get();
        }
    }
}
