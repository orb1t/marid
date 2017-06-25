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

package org.marid.jfx.beans;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractObservable implements Observable, OCleanable {

    protected final Collection<InvalidationListener> listeners = new ConcurrentLinkedQueue<>();

    public AbstractObservable() {
        OCleaner.register(this);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listeners.remove(listener);
    }

    protected void fireInvalidate(Observable observable) {
        listeners.forEach(l -> l.invalidated(observable));
    }

    @Override
    public void clean() {
        fireInvalidate(this);
    }
}
