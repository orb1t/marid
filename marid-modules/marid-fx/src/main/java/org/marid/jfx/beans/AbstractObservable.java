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
import javafx.beans.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlTransient
public abstract class AbstractObservable implements Observable {

    protected final Set<InvalidationListener> listeners = new CopyOnWriteArraySet<>();

    @Override
    public void addListener(InvalidationListener listener) {
        FxCleaner.clean(listeners);
        listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        FxCleaner.clean(listeners);
        listeners.remove(listener);
    }

    protected void fireInvalidate(Observable observable) {
        FxCleaner.clean(listeners);
        listeners.forEach(l -> l.invalidated(observable));
    }
}
