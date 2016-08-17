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

package org.marid.spring.xml.data;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.marid.io.FastArrayOutputStream;
import org.marid.misc.Casts;
import org.marid.spring.xml.MaridDataFactory;

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
public interface AbstractData<T extends AbstractData<T>> extends Externalizable, Observable {

    @Override
    default void addListener(InvalidationListener listener) {
        MaridDataFactory.addListener(this, listener);
    }

    @Override
    default void removeListener(InvalidationListener listener) {
        MaridDataFactory.removeListener(this, listener);
    }

    default T copy() {
        final FastArrayOutputStream os = new FastArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(this);
        } catch (IOException x) {
            throw new CloneFailedException(x);
        }
        try (final ObjectInputStream ois = new ObjectInputStream(os.getSharedInputStream())) {
            return Casts.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException x) {
            throw new CloneFailedException(x);
        }
    }

    @Override
    default void writeExternal(ObjectOutput out) throws IOException {
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
    default void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
                MaridDataFactory.installInvalidationListeners(this);
            } catch (NoSuchFieldException x) {
                continue;
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
    }

    default Serializable toSerializable(Object o) {
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
