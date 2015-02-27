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

package org.marid.service.proto.model;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.marid.itf.Named;
import org.marid.pref.PrefCodecs;
import org.marid.service.proto.util.NestedMap;
import org.marid.util.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import static org.marid.groovy.GroovyRuntime.newShell;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractProtoObject implements Named, AutoCloseable {

    protected final String name;
    protected final GroovyShell shell;
    protected final TreeMap<String, Object> properties = new TreeMap<>();

    protected AbstractProtoObject(Object name, NestedMap parentMap, Map<String, Object> map) {
        this.name = name(name);
        this.shell = newShell(new Binding(new NestedMap(parentMap)));
        getVariables().putAll(variables(map));
    }

    protected NestedMap getVariables() {
        return (NestedMap) shell.getContext().getVariables();
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract AbstractProtoObject getParent();

    public abstract ProtoContext getContext();

    public abstract void start();

    public abstract void stop();

    public abstract boolean isRunning();

    public void restart() {
        stop();
        while (isRunning()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException x) {
                throw new IllegalStateException(x);
            }
        }
        start();
    }

    protected <T> void putProperty(Map<String, Object> map, String key, Class<T> type) {
        final Object v = map.get(key);
        if (v != null) {
            properties.put(key, PrefCodecs.castTo(v, type));
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

    protected static String name(Object name) {
        return name == null ? UUID.randomUUID().toString() : PrefCodecs.castTo(name, String.class);
    }

    protected static Map<Object, Map<String, Object>> children(Map<String, Object> map, String key) {
        return Utils.cast(map.getOrDefault(key, Collections.emptyMap()));
    }

    protected static Map<String, Object> variables(Map<String, Object> map) {
        return Utils.cast(map.getOrDefault("variables", Collections.emptyMap()));
    }
}
