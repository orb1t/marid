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
import org.marid.functions.Changer;
import org.marid.itf.Named;
import org.marid.reflect.ReflectionUtils;
import org.marid.swing.dnd.DndObject;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class Block implements Named, DndObject {

    protected final Map<Object, Set<BlockListener>> listeners = new WeakHashMap<>();

    public void addEventListener(Object source, BlockListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).add(listener);
    }

    public void removeListener(Object source, BlockListener listener) {
        listeners.computeIfAbsent(source, o -> new HashSet<>()).remove(listener);
    }

    public void removeEventListeners(Object source) {
        listeners.remove(source);
    }

    public <L extends BlockListener> void fireEvent(Class<L> t, Consumer<L> consumer) {
        listeners.values().forEach(ls -> ls.stream().filter(t::isInstance).forEach(l -> consumer.accept(t.cast(l))));
    }

    public <L extends BlockListener, T> void fire(Class<L> t, Supplier<T> s, Consumer<T> c, T nv, Changer<L, T> es) {
        final T old = s.get();
        if (!Objects.equals(old, nv)) {
            c.accept(nv);
            listeners.values().forEach(ls -> ls.stream()
                    .filter(t::isInstance)
                    .forEach(l -> es.accept(t.cast(l), old, nv)));
        }
    }

    public abstract BlockComponent createComponent();

    public Window createWindow(Window parent) {
        return null;
    }

    public boolean isStateless() {
        try {
            return getClass().getMethod("createWindow", Window.class).getDeclaringClass() == Block.class;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    protected <T> Output<T> out(String name, Supplier<T> supplier) {
        return new Output<T>() {
            @Override
            public T get() {
                return supplier.get();
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<T> getOutputType() {
                try {
                    for (final Field field : ReflectionUtils.getDeclaredFields(getBlock().getClass())) {
                        if (field.get(getBlock()) == this) {
                            return (Class<T>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        }
                    }
                    throw new IllegalStateException("Unable to infer output class");
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }

            @Override
            public Block getBlock() {
                return Block.this;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    protected <T> Input<T> in(String name, Consumer<T> consumer, Runnable resetter) {
        return new Input<T>() {
            @Override
            public void set(T value) {
                consumer.accept(value);
            }

            @Override
            public void reset() {
                resetter.run();
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<T> getInputType() {
                try {
                    for (final Field field : ReflectionUtils.getDeclaredFields(getBlock().getClass())) {
                        if (field.get(getBlock()) == this) {
                            return (Class<T>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        }
                    }
                    throw new IllegalStateException("Unable to infer output class");
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }

            @Override
            public Block getBlock() {
                return Block.this;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

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

    public interface Input<T> extends Named {

        void set(T value);

        void reset();

        Class<T> getInputType();

        Block getBlock();
    }

    public interface Output<T> extends Named {

        T get();

        Class<T> getOutputType();

        Block getBlock();
    }

    public class In<T> implements Input<T> {

        private final String name;
        private final Class<T> type;
        private final Consumer<T> consumer;
        private final Runnable resetter;

        public In(String name, Class<T> type, Consumer<T> consumer, Runnable resetter) {
            this.name = name;
            this.type = type;
            this.consumer = consumer;
            this.resetter = resetter;
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
        public void reset() {
            resetter.run();
        }

        @Override
        public Block getBlock() {
            return Block.this;
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
        public Block getBlock() {
            return Block.this;
        }

        @Override
        public T get() {
            return supplier.get();
        }
    }
}
