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

package org.marid.spring.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import org.marid.spring.xml.DElement;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface InvalidationUtils {

    static void installChangeListener(ObjectProperty<DElement<?>> property) {
        final AtomicReference<InvalidationListener> invalidationListenerRef = new AtomicReference<>();
        final ChangeListener<DElement<?>> changeListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                Stream.of(newValue.observables()).forEach(o -> o.addListener(invalidationListenerRef.get()));
            }
            if (oldValue != null) {
                Stream.of(oldValue.observables()).forEach(o -> o.removeListener(invalidationListenerRef.get()));
            }
        };
        invalidationListenerRef.set(observable -> {
            final DElement<?> element = property.get();
            property.removeListener(changeListener);
            property.set(null);
            property.set(element);
            property.addListener(changeListener);
        });
        property.addListener(changeListener);
    }
}
