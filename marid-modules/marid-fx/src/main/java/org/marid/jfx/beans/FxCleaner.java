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

import javafx.beans.WeakListener;
import org.marid.cache.MaridClassValue;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
class FxCleaner {

    private static final MaridClassValue<Field> LISTENER_FIELDS = new MaridClassValue<>(type -> {
        try {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                for (final Field field : c.getDeclaredFields()) {
                    if (field.getType() == WeakReference.class) {
                        field.setAccessible(true);
                        return () -> field;
                    }
                }
            }
            return () -> null;
        } catch (Throwable x) {
            return () -> null;
        }
    });

    static <T> void clean(Iterable<T> listeners) {
        for (final Iterator<T> iterator = listeners.iterator(); iterator.hasNext(); ) {
            final T listener = iterator.next();
            if (listener instanceof WeakListener) {
                final WeakListener weakListener = (WeakListener) listener;
                if (weakListener.wasGarbageCollected()) {
                    iterator.remove();
                }
            } else { // TODO: remove that in Java 9
                final Field field = LISTENER_FIELDS.get(listener.getClass());
                if (field != null) {
                    try {
                        final WeakReference weakReference = (WeakReference) field.get(listener);
                        if (weakReference.get() == null) {
                            iterator.remove();
                        }
                    } catch (Throwable x) {
                        // can occur in Java 9
                    }
                }
            }
        }
    }
}
