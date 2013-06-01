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

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Logger;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asType;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractService extends ConcurrentSkipListMap<String, Object> implements Service {

    private transient ThreadGroup threadGroup;
    private transient boolean running;

    protected transient final Logger log = Logger.getLogger(getClass().getCanonicalName());

    private <T> T get(Class<T> klass, String key) {
        Object value = get(key);
        return value == null ? null : asType(value, klass);
    }

    private <T> T get(Class<T> klass, String key, T def) {
        T value = get(klass, key);
        return value == null ? def : value;
    }

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
    public String toString() {
        return getLabel();
    }
}
