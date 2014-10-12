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

import org.marid.itf.Named;
import org.marid.swing.dnd.DndObject;

import java.awt.*;
import java.util.EventListener;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Block extends Named, DndObject {

    void addEventListener(Object source, EventListener listener);

    void removeListener(Object source, EventListener listener);

    void removeEventListeners(Object source);

    <L extends EventListener> void fireEvent(Class<L> t, Consumer<L> consumer);

    <L extends EventListener, T> void fire(Class<L> t, Supplier<T> s, Consumer<T> c, T nv, BiConsumer<L, T> es);

    BlockComponent createComponent();

    default void reset() {
    }

    default boolean isStateless() {
        try {
            return getClass().getMethod("reset").getDeclaringClass() == Block.class;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    default boolean isConfigurable() {
        try {
            return getClass().getMethod("createWindow", Window.class).getDeclaringClass() == Block.class;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    List<Input> getInputs();

    List<Output> getOutputs();

    interface Input extends Named {

        void set(Object value);

        Class<?> getInputType();

        Block getBlock();

        boolean isRequired();
    }

    interface Output extends Named {

        Object get();

        Class<?> getOutputType();

        Block getBlock();
    }
}
