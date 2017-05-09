/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring.xml;

import javafx.beans.property.Property;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.marid.jfx.beans.AbstractObservable;

import javax.xml.bind.annotation.XmlTransient;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlTransient
public abstract class AbstractData<T extends AbstractData<T>> extends AbstractObservable implements Externalizable, Cloneable {

    @SuppressWarnings({"CloneDoesntCallSuperClone", "unchecked"})
    @Override
    public T clone() {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(this);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        final TreeMap<String, Object> map = new TreeMap<>();
        for (final Field field : getClass().getFields()) {
            if (!isTransient(field.getModifiers()) && !isStatic(field.getModifiers())) {
                try {
                    map.put(field.getName(), toSerializable(field.get(this)));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
        out.writeObject(map);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final TreeMap<?, ?> map = (TreeMap<?, ?>) in.readObject();
        for (final Entry<?, ?> e : map.entrySet()) {
            try {
                final Field field = getClass().getField(e.getKey().toString());
                if (Property.class.isAssignableFrom(field.getType())) {
                    final Method setValue = WritableValue.class.getMethod("setValue", Object.class);
                    setValue.invoke(field.get(this), e.getValue());
                } else if (ObservableList.class.isAssignableFrom(field.getType())) {
                    final Method addAll = List.class.getMethod("addAll", Collection.class);
                    addAll.invoke(field.get(this), e.getValue());
                } else if (ObservableMap.class.isAssignableFrom(field.getType())) {
                    final Method putAll = Map.class.getMethod("putAll", Map.class);
                    putAll.invoke(field.get(this), e.getValue());
                }
            } catch (NoSuchFieldException x) {
                throw new InvalidClassException(x.getMessage());
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
    }

    private Serializable toSerializable(Object o) {
        if (o == null || o instanceof Serializable) {
            return (Serializable) o;
        } else if (o instanceof ObservableList<?>) {
            final ObservableList<?> list = (ObservableList<?>) o;
            return list.stream().map(this::toSerializable).collect(toCollection(ArrayList::new));
        } else if (o instanceof ObservableMap<?, ?>) {
            final Map<?, ?> m = (ObservableMap<?, ?>) o;
            return m.entrySet().stream().collect(toMap(Entry::getKey,
                    e -> toSerializable(e.getValue()), (u, v) -> v, LinkedHashMap::new));
        } else if (o instanceof Property<?>) {
            return toSerializable(((Property<?>) o).getValue());
        } else {
            throw new IllegalArgumentException("Not serializable value " + o);
        }
    }
}
