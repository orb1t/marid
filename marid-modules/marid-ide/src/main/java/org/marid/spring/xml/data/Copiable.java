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
import javafx.collections.ObservableList;
import org.marid.util.Utils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Copiable<T extends Copiable<T>> extends Cloneable {

    default T copy() {
        try {
            final T instance = Utils.cast(getClass().newInstance());
            for (final Field field : getClass().getFields()) {
                if (Property.class.isAssignableFrom(field.getType())) {
                    final Property<Object> oldProp = Utils.cast(field.get(this));
                    final Property<Object> newProp = Utils.cast(field.get(instance));
                    final Object oldValue = oldProp.getValue();
                    if (oldValue instanceof Copiable<?>) {
                        newProp.setValue(((Copiable<?>) oldValue).copy());
                    } else {
                        newProp.setValue(oldValue);
                    }
                } else if (ObservableList.class.isAssignableFrom(field.getType())) {
                    final List<Object> oldList = Utils.cast(field.get(this));
                    final List<Object> newList = Utils.cast(field.get(instance));
                    for (final Object e : oldList) {
                        if (e instanceof Copiable<?>) {
                            newList.add(((Copiable<?>) e).copy());
                        } else if (e instanceof Number || e instanceof String || e instanceof Date) {
                            newList.add(e);
                        }
                    }
                }
            }
            return instance;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }
}
