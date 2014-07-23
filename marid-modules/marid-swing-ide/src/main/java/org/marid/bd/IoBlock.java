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

package org.marid.bd;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IoBlock<I, O> extends StandardBlock {

    protected final Class<I> inputType;
    protected final Class<O> outputType;

    protected final Input<I> input = new Input<I>() {
        @Override
        public void set(I value) {
            IoBlock.this.set(value);
        }

        @Override
        public Class<I> getInputType() {
            return inputType;
        }

        @Override
        public Block getBlock() {
            return IoBlock.this;
        }

        @Override
        public String getName() {
            return "in";
        }
    };

    protected final Output<O> output = new Output<O>() {
        @Override
        public O get() {
            return IoBlock.this.get();
        }

        @Override
        public Class<O> getOutputType() {
            return outputType;
        }

        @Override
        public Block getBlock() {
            return IoBlock.this;
        }

        @Override
        public String getName() {
            return "out";
        }
    };

    public IoBlock(String name, String iconText, String label, Color color, Class<I> inputType, Class<O> outputType) {
        super(name, iconText, label, color);
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.singletonList(input);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(output);
    }

    protected abstract void reset();

    protected abstract void set(I value);

    protected abstract O get();
}
