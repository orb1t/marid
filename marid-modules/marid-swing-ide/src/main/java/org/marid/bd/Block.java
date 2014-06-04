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
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class Block implements Named, Serializable, DndObject {

    protected final Map<Class<?>, List<BlockEventListener>> listeners = new IdentityHashMap<>();

    public <T extends BlockEvent, L extends BlockEventListener<T>> void addEventListener(Class<L> type, L listener) {
        listeners.computeIfAbsent(type, t -> new ArrayList<>()).add(listener);
    }

    public <T extends BlockEvent, L extends BlockEventListener<T>> void removeEventListener(Class<L> type, L listener) {
        listeners.computeIfAbsent(type, t -> new ArrayList<>()).remove(listener);
        listeners.computeIfPresent(type, (t, l) -> l.isEmpty() ? null : l);
    }

    public void removeEventListeners(Class<? extends BlockEventListener<?>> type) {
        listeners.remove(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEvent, L extends BlockEventListener<T>> void fireEvent(Class<L> type, T event) {
        listeners.getOrDefault(type, Collections.emptyList()).forEach(l -> l.listen(event));
    }

    public abstract BlockComponent createComponent();

    public abstract Window createWindow();

    public abstract List<Input<?>> getInputs();

    public abstract List<Output<?>> getOutputs();

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

    public abstract class BlockEvent extends EventObject {

        public BlockEvent() {
            super(Block.this);
        }

        @Override
        public Block getSource() {
            return (Block) super.getSource();
        }
    }

    public class PropertyChangeEvent<T> extends BlockEvent {

        public final T oldValue;
        public final T newValue;

        public PropertyChangeEvent(T oldValue, T newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    public interface BlockEventListener<T extends BlockEvent> extends EventListener {

        void listen(T blockEvent);
    }
}
