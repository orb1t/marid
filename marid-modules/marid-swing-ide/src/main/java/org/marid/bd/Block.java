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
import org.marid.logging.LogSupport;
import org.marid.util.CollectionUtils;
import org.marid.util.Utils;
import org.marid.xml.XmlPersister;

import javax.swing.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.marid.ide.MaridIde.CONTEXT;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public abstract class Block implements Named, Serializable, LogSupport, BuildTrigger {

    @XmlAttribute
    @XmlID
    protected String id = Utils.textUid();

    protected final Set<EventListener> listeners = Collections.newSetFromMap(new IdentityHashMap<>());

    public String getId() {
        return id;
    }

    public void addEventListener(EventListener listener) {
        if (listeners.add(listener)) {
            Log.log(Logger.getLogger(id), INFO, "Added {0}", null, listener);
        }
    }

    public void removeListener(EventListener listener) {
        if (listeners.remove(listener)) {
            Log.log(Logger.getLogger(id), INFO, "Removed {0}", null, listener);
        }
    }

    public <L extends EventListener> void fireEvent(Class<L> t, Consumer<L> consumer) {
        listeners.stream().filter(t::isInstance).map(t::cast).forEach(consumer);
    }

    public List<In> getInputs() {
        final List<In> list = new ArrayList<>();
        for (final Field field : getClass().getFields()) {
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (In.class.isAssignableFrom(field.getType())) {
                try {
                    list.add((In) field.get(this));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        return list.isEmpty() ? Collections.emptyList() : list;
    }

    public List<Out> getOutputs() {
        final List<Out> list = new ArrayList<>();
        for (final Field field : getClass().getFields()) {
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Out.class.isAssignableFrom(field.getType())) {
                try {
                    list.add((Out) field.get(this));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        return list.isEmpty() ? Collections.emptyList() : list;
    }

    public void transfer(Collection<BlockLink> links) {
        getInputs().forEach(i -> {
            if (i.getInputType().isArray()) {
                i.set(links.stream()
                        .filter(l -> l.getBlockInput() == i)
                        .flatMap(l -> l.getBlockOutput().getOutputType().isArray()
                                ? Arrays.stream((Object[]) l.getBlockOutput().get())
                                : Collections.singletonList(l.getBlockOutput().get()).stream())
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
        CONTEXT.getBean(XmlPersister.class).save(this, new StreamResult(bos));
        return new BlockProxy(bos.toByteArray());
    }

    @Override
    public String toString() {
        final StringWriter writer = new StringWriter();
        CONTEXT.getBean(XmlPersister.class).save(this, new StreamResult(writer));
        return writer.toString();
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
                final Block block = CONTEXT.getBean(XmlPersister.class).load(Block.class, new StreamSource(new ByteArrayInputStream(data)));
                block.id = Utils.textUid();
                return block;
            } catch (Exception x) {
                final StreamCorruptedException streamCorruptedException = new StreamCorruptedException("Resolve error");
                streamCorruptedException.initCause(x);
                throw streamCorruptedException;
            }
        }
    }

    public class In {

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

        public boolean isRequired() {
            return required;
        }

        public String getName() {
            return name;
        }

        public Class<?> getInputType() {
            return type;
        }

        public void set(Object value) {
            Utils.<Consumer<Object>>cast(consumer).accept(value);
        }

        public Block getBlock() {
            return Block.this;
        }
    }

    public class Out {

        private final String name;
        private final Class<?> type;
        private final Supplier<?> supplier;

        public <T> Out(String name, Class<T> type, Supplier<T> supplier) {
            this.name = name;
            this.type = type;
            this.supplier = supplier;
        }

        public String getName() {
            return name;
        }

        public Class<?> getOutputType() {
            return type;
        }

        public Block getBlock() {
            return Block.this;
        }

        public Object get() {
            return supplier.get();
        }
    }
}
