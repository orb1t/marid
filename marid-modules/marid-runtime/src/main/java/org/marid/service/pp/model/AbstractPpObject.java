/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.service.pp.model;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import org.marid.itf.Named;
import org.marid.pref.PrefCodecs;
import org.marid.service.pp.util.MapUtil;
import org.marid.service.pp.util.NestedMap;
import org.marid.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static org.marid.groovy.GroovyRuntime.newShell;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractPpObject implements Named, AutoCloseable {

    protected final String name;
    protected final GroovyShell shell;
    protected final TreeMap<String, Object> properties = new TreeMap<>();

    protected AbstractPpObject(Object name, NestedMap parentMap, Map<String, Object> map) {
        this.name = MapUtil.name(name);
        this.shell = newShell(new Binding(new NestedMap(parentMap)));
        getVariables().putAll(MapUtil.variables(map));
        getVariables().put("self", this);
    }

    protected NestedMap getVariables() {
        return (NestedMap) shell.getContext().getVariables();
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract AbstractPpObject getParent();

    public abstract PpContext getContext();

    public abstract void start();

    public abstract void stop();

    public abstract boolean isRunning();

    public abstract boolean isStarted();

    public abstract boolean isStopped();

    public synchronized void restart() {
        stop();
        start();
    }

    protected <T> void putProperty(Map<String, Object> map, String key, Class<T> type) {
        final Object v = map.get(key);
        if (v != null) {
            if (type.isInstance(v)) {
                properties.put(key, v);
            } else if (v instanceof Closure) {
                if (type.isInterface() && type.isAnnotationPresent(FunctionalInterface.class)) {
                    properties.put(key, PrefCodecs.castTo(v, type));
                } else {
                    properties.put(key, PrefCodecs.castTo(((Closure) v).call(this), type));
                }
            } else {
                properties.put(key, PrefCodecs.castTo(v, type));
            }
        }
    }

    protected <T> T getProperty(String key, Supplier<? extends T> supplier) {
        final Object v = properties.get(key);
        if (v != null) {
            return Utils.cast(v);
        } else {
            if (getParent() != null) {
                return getParent().getProperty(key, supplier);
            } else {
                return supplier.get();
            }
        }
    }

    public List<String> getPath() {
        final List<String> path = new ArrayList<>();
        for (AbstractPpObject o = this; o != null; o = o.getParent()) {
            path.add(0, o.getName());
        }
        return path;
    }

    @Override
    public String toString() {
        return String.join("/", getPath());
    }
}
