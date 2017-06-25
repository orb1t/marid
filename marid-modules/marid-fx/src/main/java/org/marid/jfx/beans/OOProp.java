package org.marid.jfx.beans;

/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * @author Dmitry Ovchinnikov
 */
public class OOProp<T extends Observable> extends OProp<T> {

    private final InvalidationListener listener = o -> fireChanged(false);

    public OOProp(String name) {
        super(name);
    }

    public OOProp(String name, T value) {
        super(name, value);
    }

    @Override
    protected void onChange(T oldValue, T newValue) {
        if (oldValue != null) {
            oldValue.removeListener(listener);
        }
        if (newValue != null) {
            newValue.addListener(listener);
        }
    }
}
