/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.services;

import org.marid.typecast.ParameterizedObject;

import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractService extends ParameterizedObject implements Service {

    private static final long serialVersionUID = -5619464299660713815L;
    private transient ThreadGroup threadGroup;
    private transient boolean running;

    protected transient final Logger log = Logger.getLogger(getClass().getCanonicalName());

    @Override
    public String getName() {
        return get(String.class, "name", getClass().getSimpleName());
    }

    @Override
    public synchronized ThreadGroup getThreadGroup() {
        if (threadGroup == null) {
            String name = get(String.class, "threadGroupName", getName());
            return threadGroup = new ThreadGroup(name);
        } else {
            return threadGroup;
        }
    }

    @Override
    public String getType() {
        String type = get(String.class, "type");
        if (type == null) {
            type = getClass().getSimpleName();
            if (type.endsWith("Service")) {
                return type.substring(0, type.length() - 7).toLowerCase();
            } else {
                return type.toLowerCase();
            }
        } else {
            return type;
        }
    }

    @Override
    public String getLabel() {
        return get(String.class, "label", getName());
    }

    @Override
    public String getVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    protected abstract void doStart() throws Exception;

    @Override
    public synchronized void start() throws Exception {
        if (!running) {
            doStart();
            running = true;
        }
    }

    protected abstract void doStop() throws Exception;

    @Override
    public synchronized void stop() throws Exception {
        if (running) {
            doStop();
            running = false;
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
