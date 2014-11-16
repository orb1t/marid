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

import org.marid.ide.components.BlockPersister;
import org.marid.itf.Named;
import org.marid.util.CollectionUtils;
import org.marid.util.Utils;

import javax.swing.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public abstract class Block implements Named, Serializable {

    @XmlAttribute
    @XmlID
    protected String id = Utils.textUid();

    protected final Map<Object, Set<EventListener>> listeners = new WeakHashMap<>();

    public String getId() {
        return id;
    }

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

    public <L extends EventListener, T> void fire(Class<L> t, Supplier<T> s, Consumer<T> c, T nv, BiConsumer<L, T> ch) {
        final T old = s.get();
        if (!Objects.equals(old, nv)) {
            c.accept(nv);
            listeners.values().forEach(ls -> ls.stream()
                    .filter(t::isInstance)
                    .forEach(l -> ch.accept(t.cast(l), nv)));
        }
    }

    public <L extends EventListener, T> void fire(Class<L> t, AtomicReference<T> ref, T nv, BiConsumer<L, T> ch) {
        final T old = ref.getAndSet(nv);
        if (!Objects.deepEquals(old, nv)) {
            listeners.values().forEach(ls -> ls.stream().filter(t::isInstance).forEach(l -> ch.accept(t.cast(l), nv)));
        }
    }

    public abstract List<Input> getInputs();

    public abstract List<Output> getOutputs();

    public List<Output> getExports() {
        return Collections.emptyList();
    }

    public void transfer(Collection<BlockLink> links) {
        getInputs().forEach(i -> {
            if (i.getInputType().isArray()) {
                i.set(links.stream()
                        .filter(l -> l.getBlockInput() == i)
                        .flatMap(l -> l.getBlockOutput().getOutputType().isArray()
                                ? Arrays.stream((Object[]) l.getBlockOutput().get())
                                : Arrays.asList(l.getBlockOutput().get()).stream())
                        .toArray(CollectionUtils.getArrayFunction(i.getInputType().getComponentType())));
            } else {
                links.stream()
                        .filter(l -> l.getBlockInput() == i)
                        .forEach(l -> l.getBlockInput().set(l.getBlockOutput().get()));
            }
        });
    }

    public ImageIcon getVisualRepresentation() {
        return null;
    }

    public ImageIcon getVisualRepresentation(int width, int height) {
        final ImageIcon icon = getVisualRepresentation();
        if (icon == null || icon.getIconWidth() == width && icon.getIconHeight() == height) {
            return icon;
        } else {
            return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }
    }

    public abstract BlockComponent createComponent();

    protected Object writeReplace() throws ObjectStreamException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BlockPersister.instance.save(this, new StreamResult(bos));
            return new BlockProxy(bos.toByteArray());
        } catch (IOException x) {
            throw new WriteAbortedException("Replace error", x);
        }
    }

    @Override
    public String toString() {
        final BlockPersister persister = BlockPersister.instance;
        if (persister == null) {
            return super.toString();
        } else {
            final StringWriter writer = new StringWriter();
            try {
                persister.save(this, new StreamResult(writer));
                return writer.toString();
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        }
    }

    public void reset() {
    }

    protected static class BlockProxy implements Serializable {

        private final byte[] data;

        public BlockProxy(byte[] data) {
            this.data = data;
        }

        public Object readResolve() throws ObjectStreamException {
            try {
                final Block block = BlockPersister.instance.load(new ByteArrayInputStream(data));
                block.id = Utils.textUid();
                return block;
            } catch (Exception x) {
                final StreamCorruptedException streamCorruptedException = new StreamCorruptedException("Resolve error");
                streamCorruptedException.initCause(x);
                throw streamCorruptedException;
            }
        }
    }

    public interface Input extends Named {

        void set(Object value);

        Class<?> getInputType();

        Block getBlock();

        boolean isRequired();
    }

    public interface Output extends Named {

        Object get();

        Class<?> getOutputType();

        Block getBlock();
    }

    public class In implements Input {

        private final String name;
        private final Class<?> type;
        private final boolean required;
        private final Consumer<?> consumer;

        public <T> In(String name, Class<T> type, boolean required, Consumer<T> consumer) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.consumer = consumer;
        }

        public <T> In(String name, Class<T> type, Consumer<T> consumer) {
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
        public Class<?> getInputType() {
            return type;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void set(Object value) {
            ((Consumer) consumer).accept(value);
        }

        @Override
        public Block getBlock() {
            return Block.this;
        }
    }

    public class Out implements Output {

        private final String name;
        private final Class<?> type;
        private final Supplier<?> supplier;

        public <T> Out(String name, Class<T> type, Supplier<T> supplier) {
            this.name = name;
            this.type = type;
            this.supplier = supplier;
        }

        @Override
        public String getName() {
            return name;
        }

        public Class<?> getOutputType() {
            return type;
        }

        @Override
        public Block getBlock() {
            return Block.this;
        }

        @Override
        public Object get() {
            return supplier.get();
        }
    }
}
