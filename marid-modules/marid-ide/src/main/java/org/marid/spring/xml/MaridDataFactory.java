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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.marid.spring.xml.data.AbstractData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridDataFactory {

    private static final Map<AbstractData, List<InvalidationListener>> LISTENERS = new WeakHashMap<>();

    public static void addListener(AbstractData data, InvalidationListener listener) {
        final List<InvalidationListener> listeners = LISTENERS.computeIfAbsent(data, k -> new ArrayList<>());
        listeners.add(listener);
    }

    public static void removeListener(AbstractData data, InvalidationListener listener) {
        LISTENERS.computeIfPresent(data, (d, ls) -> {
            ls.remove(listener);
            return ls.isEmpty() ? null : ls;
        });
    }

    public static void invalidate(AbstractData data) {
        final List<InvalidationListener> listeners = LISTENERS.get(data);
        if (listeners != null) {
            listeners.forEach(listener -> listener.invalidated(data));
        }
    }

    public static void installInvalidationListeners(AbstractData data) {
        for (final Field field : data.getClass().getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && Observable.class.isAssignableFrom(field.getType())) {
                try {
                    final Observable observable = (Observable) field.get(data);
                    observable.addListener(o -> invalidate(data));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
        }
    }

    public static <T extends AbstractData> T create(Class<T> type) {
        try {
            final T data = type.newInstance();
            installInvalidationListeners(data);
            return data;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }
}
