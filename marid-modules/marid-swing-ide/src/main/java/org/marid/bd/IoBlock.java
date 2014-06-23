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

import org.marid.bd.components.StandardBlockComponent;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IoBlock<I, O> extends StandardBlock {

    protected final String inputName;
    protected final String outputName;

    protected final Input<I> input = new Input<I>() {
        @Override
        public void set(I value) {
            IoBlock.this.set(value);
        }

        @Override
        public void reset() {
            IoBlock.this.reset();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<I> getInputType() {
            for (final Type type : IoBlock.this.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Input.class) {
                    return (Class<I>) ((ParameterizedType) type).getActualTypeArguments()[0];
                }
            }
            throw new IllegalStateException("Unable to infer input type");
        }

        @Override
        public Block getBlock() {
            return IoBlock.this;
        }

        @Override
        public String getName() {
            return inputName;
        }
    };

    protected final Output<O> output = new Output<O>() {
        @Override
        public O get() {
            return IoBlock.this.get();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<O> getOutputType() {
            for (final Type type : IoBlock.this.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Output.class) {
                    return (Class<O>) ((ParameterizedType) type).getActualTypeArguments()[0];
                }
            }
            throw new IllegalStateException("Unable to infer input type");
        }

        @Override
        public Block getBlock() {
            return IoBlock.this;
        }

        @Override
        public String getName() {
            return outputName;
        }
    };

    public IoBlock(String name, String inputName, String outputName, String icon) {
        super(name, icon);
        this.inputName = inputName;
        this.outputName = outputName;
    }

    public IoBlock(String name, String inputName, String outputName, ImageIcon icon) {
        super(name, icon);
        this.inputName = inputName;
        this.outputName = outputName;
    }

    @Override
    public BlockComponent createComponent() {
        return new StandardBlockComponent<>(this, c -> c.add(new JLabel(getVisualRepresentation())));
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
