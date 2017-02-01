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

package org.marid.jfx.beans;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ForwardingProperty<T, V extends Property<T>>
        extends ForwardingObservableValue<T, V> implements Property<T>, ObservableObjectValue<T>, WritableObjectValue<T> {

    public ForwardingProperty(V delegate) {
        super(delegate);
    }

    @Override
    public void bind(ObservableValue<? extends T> observable) {
        delegate.bind(observable);
    }

    @Override
    public void unbind() {
        delegate.unbind();
    }

    @Override
    public boolean isBound() {
        return delegate.isBound();
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        delegate.bindBidirectional(other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        delegate.unbindBidirectional(other);
    }

    @Override
    public Object getBean() {
        return delegate.getBean();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setValue(T value) {
        delegate.setValue(value);
    }

    @Override
    public T get() {
        return getValue();
    }

    @Override
    public void set(T value) {
        setValue(value);
    }
}
