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

import javafx.beans.property.Property;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.marid.io.FastArrayOutputStream;
import org.marid.misc.Casts;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractData<T extends AbstractData<T>> implements Cloneable, Externalizable {

    public abstract void save(Node node, Document document);

    public abstract void load(Node node, Document document);

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public T clone() {
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
    public int hashCode() {
        final FastArrayOutputStream os = new FastArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(this);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
        return Arrays.hashCode(os.getSharedBuffer());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            final FastArrayOutputStream thisOs = new FastArrayOutputStream(), thatOs = new FastArrayOutputStream();
            try (final ObjectOutputStream thisOos = new ObjectOutputStream(thisOs);
                 final ObjectOutputStream thatOos = new ObjectOutputStream(thatOs)) {
                thisOos.writeObject(this);
                thatOos.writeObject(obj);
            } catch (IOException x) {
                throw new IllegalStateException(x);
            }
            return Arrays.equals(thisOs.getSharedBuffer(), thatOs.getSharedBuffer());
        }
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final Field field : getClass().getFields()) {
            try {
                if (Property.class.isAssignableFrom(field.getType())) {
                    final Property<?> property = (Property<?>) field.get(this);
                    map.put(property.getName(), property.getValue());
                } else {
                    map.put(field.getName(), field.get(this));
                }
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
        return getClass().getSimpleName() + map;
    }
}
