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

import org.marid.bd.components.IoBlockComponent;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class IoBlock<I, O> extends StandardBlock implements Block.InputOutput<I, O> {

    protected final String buttonName;

    public IoBlock(String name, String buttonName, String icon) {
        super(name, icon);
        this.buttonName = buttonName;
    }

    public IoBlock(String name, String buttonName, ImageIcon icon) {
        super(name, icon);
        this.buttonName = buttonName;
    }

    @Override
    public BlockComponent createComponent() {
        return new IoBlockComponent<>(this);
    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.singletonList(this);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(this);
    }

    @Override
    public Block getBlock() {
        return this;
    }

    public String getButtonName() {
        return buttonName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<I> getInputType() {
        for (final Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == InputOutput.class) {
                return (Class<I>) ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        throw new IllegalStateException("Unable to infer input type");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<O> getOutputType() {
        for (final Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == InputOutput.class) {
                return (Class<O>) ((ParameterizedType) type).getActualTypeArguments()[1];
            }
        }
        throw new IllegalStateException("Unable to infer input type");
    }
}
