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

package org.marid.service.proto;

import org.marid.Marid;
import org.marid.dyn.Casting;
import org.marid.groovy.MapProxies;
import org.marid.itf.Named;
import org.marid.methods.LogMethods;
import org.marid.service.util.MapUtil;
import org.marid.util.Utils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ProtoObject extends Observable implements Named, AutoCloseable {

    protected final ProtoObject parent;
    protected final String name;
    protected final Consumer<ProtoObject> onInit;

    protected ProtoObject(ProtoObject parent, Object name, Map<String, Object> map) {
        this.parent = parent;
        this.name = MapUtil.name(name);
        this.onInit = Utils.cast(f(map, "onInit", Consumer.class, v -> {}));
    }

    protected void init() {
        onInit.accept(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public ProtoObject getParent() {
        return parent;
    }

    public abstract void start();

    public abstract void stop();

    public synchronized void restart() {
        stop();
        start();
    }

    public abstract boolean isRunning();

    public abstract boolean isStarted();

    public abstract boolean isStopped();

    public List<String> getPath() {
        final List<String> path = new ArrayList<>();
        for (ProtoObject o = this; o != null; o = o.getParent()) {
            path.add(0, o.getName());
        }
        return path;
    }

    public abstract ProtoObject getContext();

    protected <T> T f(Map<String, Object> map, String key, Class<T> type, T defaultValue) {
        final Object v = map.get(key);
        if (v != null) {
            if (type.isInstance(v)) {
                return type.cast(v);
            } else if (type.isInterface() && v instanceof Map) {
                return MapProxies.newInstance(type, (Map) v);
            } else {
                return Casting.castTo(type, v);
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver((observable, arg) -> {
            try {
                o.update(observable, arg);
            } catch (Exception x) {
                LogMethods.severe(Marid.LOGGER, "Observer error in {0} : {1}", x, observable, arg);
            }
        });
    }

    @Override
    public String toString() {
        return String.join("/", getPath());
    }
}
