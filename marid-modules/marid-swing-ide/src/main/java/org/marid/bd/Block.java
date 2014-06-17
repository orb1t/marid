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

import org.marid.beans.MaridBeans;
import org.marid.itf.Named;
import org.marid.swing.dnd.DndObject;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class Block implements Named, Serializable, DndObject {

    protected final Map<Object, Set<BlockEventListener>> listeners = new WeakHashMap<>();

    public void addEventListener(Object source, BlockEventListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).add(listener);
    }

    public void removeListener(Object source, BlockEventListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).remove(listener);
    }

    public void removeEventListeners(Object source) {
        listeners.remove(source);
    }

    public <L extends BlockEventListener> void fireEvent(Class<L> t, Consumer<L> consumer) {
        listeners.values().forEach(ls -> ls.stream().filter(t::isInstance).forEach(l -> consumer.accept(t.cast(l))));
    }

    public abstract BlockComponent createComponent();

    public abstract Window createWindow(Window parent);

    public abstract List<Input<?>> getInputs();

    public abstract List<Output<?>> getOutputs();

    protected Object writeReplace() throws ObjectStreamException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MaridBeans.write(bos, this);
        return new BlockProxy(getClass(), bos.toByteArray());
    }

    protected static class BlockProxy implements Serializable {

        private final Class<?> type;
        private final byte[] data;

        public BlockProxy(Class<?> type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        public Object readResolve() throws ObjectStreamException {
            return MaridBeans.read(type, new ByteArrayInputStream(data));
        }
    }

    public abstract class Port<T> implements Named {

        public abstract Class<T> getType();

        public Block getBlock() {
            return Block.this;
        }
    }

    public abstract class Input<T> extends Port<T> {

        public abstract void set(T value);

        @Override
        public String toString() {
            return getBlock().getName() + "<-" + getName();
        }
    }

    public class In<T> extends Input<T> {

        private final String name;
        private final Class<T> type;
        private final Consumer<T> consumer;

        public In(String name, Class<T> type, Consumer<T> consumer) {
            this.name = name;
            this.type = type;
            this.consumer = consumer;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void set(T value) {
            consumer.accept(value);
        }

        @Override
        public Class<T> getType() {
            return type;
        }
    }

    public abstract class Output<T> extends Port<T> {

        public abstract T get();

        @Override
        public String toString() {
            return getBlock().getName() + "->" + getName();
        }
    }

    public class Out<T> extends Output<T> {

        private final String name;
        private final Class<T> type;
        private final Supplier<T> supplier;

        public Out(String name, Class<T> type, Supplier<T> supplier) {
            this.name = name;
            this.type = type;
            this.supplier = supplier;
        }

        @Override
        public String getName() {
            return name;
        }

        public Class<T> getType() {
            return type;
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }
}
