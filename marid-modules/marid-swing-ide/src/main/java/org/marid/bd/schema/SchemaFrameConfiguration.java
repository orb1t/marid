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

package org.marid.bd.schema;

import org.marid.swing.InputMaskType;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.EnumInputControl;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "mouse")
public interface SchemaFrameConfiguration extends Configuration {

    @Input(tab = "mouse")
    Pv<InputMaskType> PAN = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> InputMaskType.SHIFT);

    @Input(tab = "mouse")
    Pv<InputMaskType> MOVE = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> InputMaskType.ALT);

    @Input(tab = "mouse")
    Pv<InputMaskType> DRAG = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> InputMaskType.CONTROL);
}
