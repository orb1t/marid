/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface ConstantValue<T> extends ObservableObjectValue<T> {

    @Override
    default T getValue() {
        return get();
    }

    @Override
    default void addListener(ChangeListener<? super T> listener) {
    }

    @Override
    default void removeListener(ChangeListener<? super T> listener) {
    }

    @Override
    default void addListener(InvalidationListener listener) {
    }

    @Override
    default void removeListener(InvalidationListener listener) {
    }

    static <T> ConstantValue<T> value(T value) {
        return () -> value;
    }

    static <T> ConstantValue<T> value(ConstantValue<T> value) {
        return value;
    }

    static <T> void bind(Property<T> property, ConstantValue<T> value) {
        property.bind(value);
    }
}
