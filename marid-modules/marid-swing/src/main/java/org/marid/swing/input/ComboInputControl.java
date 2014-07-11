/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.input;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Vector;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;

/**
 * @author Dmitry Ovchinnikov
 */
public class ComboInputControl<E> extends JComboBox<E> implements InputControl<E> {

    public ComboInputControl(E[] values) {
        super(values);
    }

    public ComboInputControl(Collection<E> values) {
        super(new Vector<>(values));
    }

    public ComboInputControl(Vector<E> values) {
        super(values);
    }

    public ComboInputControl(Class<E> type, Class<?> constantContainer) {
        super(vectorFrom(type, constantContainer));
    }

    public ComboInputControl(Class<E> type) {
        super(vectorFrom(type, type));
    }

    @Override
    public E getInputValue() {
        return getModel().getElementAt(getSelectedIndex());
    }

    @Override
    public void setInputValue(E value) {
        getModel().setSelectedItem(value);
    }

    private static <E> Vector<E> vectorFrom(Class<E> type, Class<?> constantContainer) {
        try {
            final Vector<E> vector = new Vector<>();
            for (final Field f : constantContainer.getFields()) {
                if (isStatic(f.getModifiers()) && isPublic(f.getModifiers()) && type.isAssignableFrom(f.getType())) {
                    vector.add(type.cast(f.get(null)));
                }
            }
            return vector;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }
}
