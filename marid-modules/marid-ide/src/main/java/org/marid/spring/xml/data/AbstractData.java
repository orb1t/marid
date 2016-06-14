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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.marid.util.Utils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractData<T extends AbstractData<T>> implements Cloneable {

    @Override
    public T clone() {
        try {
            final T instance = Utils.cast(getClass().newInstance());
            for (final Field field : getClass().getFields()) {
                if (javafx.beans.property.Property.class.isAssignableFrom(field.getType())) {
                    final Property<Object> oldProp = Utils.cast(field.get(this));
                    final Property<Object> newProp = Utils.cast(field.get(instance));
                    final Object oldValue = oldProp.getValue();
                    if (oldValue instanceof AbstractData<?>) {
                        newProp.setValue(((AbstractData<?>) oldValue).clone());
                    } else {
                        newProp.setValue(oldValue);
                    }
                } else if (ObservableList.class.isAssignableFrom(field.getType())) {
                    final List<Object> oldList = Utils.cast(field.get(this));
                    final List<Object> newList = Utils.cast(field.get(instance));
                    for (final Object e : oldList) {
                        if (e instanceof AbstractData<?>) {
                            newList.add(((AbstractData<?>) e).clone());
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

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        try {
            for (final Field field : getClass().getFields()) {
                final Object fieldValue = field.get(this);
                if (fieldValue instanceof Property<?>) {
                    builder.append(((Property<?>) fieldValue).getValue());
                } else if (fieldValue instanceof ObservableList<?>) {
                    ((ObservableList<?>) fieldValue).forEach(builder::append);
                } else {
                    builder.append(fieldValue);
                }
            }
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
        return builder.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        try {
            for (final Field field : getClass().getFields()) {
                final Object thisVal = field.get(this);
                final Object thatVal = field.get(obj);
                if (thisVal instanceof Property<?>) {
                    if (!Objects.equals(((Property<?>) thisVal).getValue(), ((Property<?>) thatVal).getValue())) {
                        return false;
                    }
                } else if (thisVal instanceof ObservableList<?>) {
                    final ObservableList<?> thisList = (ObservableList<?>) thisVal;
                    final ObservableList<?> thatList = (ObservableList<?>) thatVal;
                    if (thisList.size() != thatList.size()) {
                        return false;
                    } else {
                        for (int i = 0; i < thisList.size(); i++) {
                            final Object thisElem = thisList.get(i);
                            final Object thatElem = thatList.get(i);
                            if (!Objects.equals(thisElem, thatElem)) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (!Objects.equals(thisVal, thatVal)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }
}
