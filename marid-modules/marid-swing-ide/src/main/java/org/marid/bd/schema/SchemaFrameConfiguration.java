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

import org.marid.bd.shapes.LinkShapeType;
import org.marid.swing.InputMaskType;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Form;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.ComboInputControl;
import org.marid.swing.input.ExtComboInputControl;

import static org.marid.bd.shapes.LinkShapeType.LINE;
import static org.marid.swing.InputMaskType.CONTROL;
import static org.marid.swing.InputMaskType.SHIFT;

/**
 * @author Dmitry Ovchinnikov
 */
@Form(name = "BD configuration")
@Tab(node = "mouse")
@Tab(node = "links")
public interface SchemaFrameConfiguration extends Configuration {

    @Input(tab = "mouse")
    Pv<InputMaskType> PAN = new Pv<>(() -> new ComboInputControl<>(InputMaskType.class), () -> SHIFT);

    @Input(tab = "mouse")
    Pv<InputMaskType> DRAG = new Pv<>(() -> new ComboInputControl<>(InputMaskType.class), () -> CONTROL);

    @Input(tab = "links")
    Pv<LinkShapeType> LINK_SHAPE_TYPE = new Pv<>(() -> new ExtComboInputControl<>(LinkShapeType.class), () -> LINE);
}
