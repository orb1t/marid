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

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IoBlock extends StandardBlock {

    protected final Class inputType;
    protected final Class outputType;

    protected final Input input = new Input() {
        @Override
        public void set(Object value) {
            IoBlock.this.set(value);
        }

        @Override
        public Class<?> getInputType() {
            return inputType;
        }

        @Override
        public Block getBlock() {
            return IoBlock.this;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public String getName() {
            return "in";
        }
    };

    protected final Output output = new Output() {
        @Override
        public Object get() {
            return IoBlock.this.get();
        }

        @Override
        public Class<?> getOutputType() {
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

    public IoBlock(Class<?> inputType, Class<?> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public List<Input> getInputs() {
        return Collections.singletonList(input);
    }

    @Override
    public List<Output> getOutputs() {
        return Collections.singletonList(output);
    }

    protected abstract void set(Object value);

    protected abstract Object get();
}
