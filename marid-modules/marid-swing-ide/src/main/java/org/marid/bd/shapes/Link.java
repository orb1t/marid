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

package org.marid.bd.shapes;

import org.marid.bd.BlockComponent;

/**
 * @author Dmitry Ovchinnikov
 */
public class Link {

    public final BlockComponent outputComponent;
    public final String output;
    public final BlockComponent inputComponent;
    public final String input;

    public Link(BlockComponent outputComponent, String output, BlockComponent inputComponent, String input) {
        this.outputComponent = outputComponent;
        this.output = output;
        this.inputComponent = inputComponent;
        this.input = input;
    }

    public Link(BlockComponent.Output output, BlockComponent.Input input) {
        this(output.getBlockComponent(), output.getOutput().getName(), input.getBlockComponent(), input.getInput().getName());
    }

    public Link(LinkShape linkShape) {
        this(linkShape.output, linkShape.input);
    }
}
