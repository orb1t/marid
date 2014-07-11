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
import org.marid.swing.forms.Form;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.ComboInputControl;
import org.marid.swing.input.EnumInputControl;

import static org.marid.bd.shapes.LinkShapeTypes.LINE;
import static org.marid.bd.shapes.LinkShapeTypes.LinkShapeType;
import static org.marid.swing.InputMaskType.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Form(name = "BD configuration")
@Tab(node = "mouse")
@Tab(node = "links")
public interface SchemaFrameConfiguration extends Configuration {

    @Input(tab = "mouse")
    Pv<InputMaskType> PAN = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> SHIFT);

    @Input(tab = "mouse")
    Pv<InputMaskType> MOVE = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> ALT);

    @Input(tab = "mouse")
    Pv<InputMaskType> DRAG = new Pv<>(() -> new EnumInputControl<>(InputMaskType::values), () -> CONTROL);

    @Input(tab = "links")
    Pv<LinkShapeType> LINK_SHAPE_TYPE = new Pv<>(() -> new ComboInputControl<>(LinkShapeType.class), () -> LINE);
}
