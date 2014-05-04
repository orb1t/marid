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

package org.marid.ide.bde;

import org.marid.bde.view.BlockLinkType;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.EnumInputControl;
import org.marid.swing.input.FloatInputControl;
import org.marid.swing.input.SpinIntInputControl;

import static org.marid.bde.view.BlockLinkType.LINE_LINK;

/**
 * @author Dmitry Ovchinnikov.
 */
@Tab(node = "appearance")
@Tab(node = "GA")
public interface BdeConfiguration extends Configuration {

    @Input(tab = "appearance")
    Pv<BlockLinkType> LINK_TYPE = new Pv<>(() -> new EnumInputControl<>(BlockLinkType::values), () -> LINE_LINK);

    @Input(tab = "GA")
    Pv<Float> MUTATION_PROBABILITY = new Pv<>(FloatInputControl::new, () -> 0.3f);

    @Input(tab = "GA")
    Pv<Integer> INCUBATOR_SIZE = new Pv<>(() -> new SpinIntInputControl(2, 10, 1), () -> 4);

    @Input(tab = "GA")
    Pv<Integer> SPECIES = new Pv<>(() -> new SpinIntInputControl(10, 100, 5), () -> 30);
}
