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

package org.marid.ide.widgets.memory;

import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.BooleanInputControl;
import org.marid.swing.input.SpinIntInputControl;

/**
 * @author Dmitry Ovchinnikov.
 */
@Tab(node = "chart")
public interface MemoryWidgetConfiguration extends Configuration {

    @Input(tab = "chart")
    Pv<Boolean> USE_BUFFER = new Pv<>(BooleanInputControl::new, () -> true);

    @Input(tab = "chart")
    Pv<Boolean> SAVE = new Pv<>(BooleanInputControl::new, () -> true);

    @Input(tab = "chart")
    Pv<Boolean> PRINT = new Pv<>(BooleanInputControl::new, () -> true);

    @Input(tab = "chart")
    Pv<Boolean> ZOOM = new Pv<>(BooleanInputControl::new, () -> true);

    @Input(tab = "chart")
    Pv<Boolean> TOOLTIPS = new Pv<>(BooleanInputControl::new, () -> true);

    @Input(tab = "chart")
    Pv<Integer> UPDATE_INTERVAL = new Pv<>(() -> new SpinIntInputControl(1, 10, 1), () -> 1);

    @Input(tab = "chart")
    Pv<Integer> HISTORY_SIZE = new Pv<>(() -> new SpinIntInputControl(1, 60, 1), () -> 3);
}
