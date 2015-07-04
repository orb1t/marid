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

package org.marid.ide.components.conf;

import org.marid.bd.shapes.LinkShapeType;
import org.marid.dyn.MetaInfo;
import org.marid.swing.ComponentConfiguration;
import org.marid.swing.InputMaskType;
import org.marid.swing.input.ComboInputControl;
import org.marid.swing.input.ExtComboInputControl;
import org.springframework.stereotype.Component;

import static org.marid.bd.shapes.LinkShapeType.LINE;
import static org.marid.swing.InputMaskType.CONTROL;
import static org.marid.swing.InputMaskType.SHIFT;

/**
 * @author Dmitry Ovchinnikov
 */
@MetaInfo(name = "Schema Frame Configuration")
@Component
public class SchemaFrameConfiguration extends ComponentConfiguration {

    @MetaInfo(order = 1)
    public final P<InputMaskType> pan = p("pan", () -> new ComboInputControl<>(InputMaskType.class), () -> SHIFT);

    @MetaInfo(order = 2)
    public final P<InputMaskType> drag = p("drag", () -> new ComboInputControl<>(InputMaskType.class), () -> CONTROL);

    @MetaInfo(order = 3)
    public final P<LinkShapeType> link = p("link", () -> new ExtComboInputControl<>(LinkShapeType.class), () -> LINE);
}
