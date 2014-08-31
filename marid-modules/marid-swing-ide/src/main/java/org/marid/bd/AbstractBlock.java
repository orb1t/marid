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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.marid.beans.MaridBeans;
import org.marid.functions.Changer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractBlock implements Block {

    protected final Map<Object, Set<EventListener>> listeners = new WeakHashMap<>();

    public void addEventListener(Object source, EventListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).add(listener);
    }

    public void removeListener(Object source, EventListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).remove(listener);
    }

    public void removeEventListeners(Object source) {
        listeners.remove(source);
    }

    public <L extends EventListener> void fireEvent(Class<L> t, Consumer<L> consumer) {
        listeners.values().forEach(ls -> ls.stream().filter(t::isInstance).forEach(l -> consumer.accept(t.cast(l))));
    }

    public <L extends EventListener, T> void fire(Class<L> t, Supplier<T> s, Consumer<T> c, T nv, Changer<L, T> es) {
        final T old = s.get();
        if (!Objects.equals(old, nv)) {
            c.accept(nv);
            listeners.values().forEach(ls -> ls.stream()
                    .filter(t::isInstance)
                    .forEach(l -> es.accept(t.cast(l), old, nv)));
        }
    }

    protected Object writeReplace() throws ObjectStreamException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MaridBeans.write(bos, this);
        return new BlockProxy(getClass(), bos.toByteArray());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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

    public class In<T> implements Input<T> {

        private final String name;
        private final Class<T> type;
        private final boolean required;
        private final Consumer<T> consumer;

        public In(String name, Class<T> type, boolean required, Consumer<T> consumer) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.consumer = consumer;
        }

        public In(String name, Class<T> type, Consumer<T> consumer) {
            this(name, type, false, consumer);
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<T> getInputType() {
            return type;
        }

        @Override
        public void set(T value) {
            consumer.accept(value);
        }

        @Override
        public AbstractBlock getBlock() {
            return AbstractBlock.this;
        }
    }

    public class Out<T> implements Output<T> {

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

        public Class<T> getOutputType() {
            return type;
        }

        @Override
        public AbstractBlock getBlock() {
            return AbstractBlock.this;
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }
}
