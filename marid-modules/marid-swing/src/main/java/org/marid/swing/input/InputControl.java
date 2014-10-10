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

import org.marid.l10n.L10nSupport;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public interface InputControl<V> extends L10nSupport {

    V getInputValue();

    void setInputValue(V value);

    default JComponent getComponent() {
        return (JComponent) this;
    }

    default Class<?> getType() {
        for (final Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == InputControl.class) {
                return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        throw new IllegalStateException();
    }
}
