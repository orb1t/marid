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

import org.marid.concurrent.ConcurrentUtils;
import org.marid.dyn.Casting;
import org.marid.groovy.MapProxies;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.marid.service.util.MapUtil;
import org.marid.util.Utils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ProtoObject implements Named, LogSupport, AutoCloseable {

    protected final List<ProtoEventListener> eventListeners = new CopyOnWriteArrayList<>();
    protected final ProtoObject parent;
    protected final String name;
    protected final Consumer<ProtoObject> onInit;
    protected final Boolean delegateLogging;
    protected final Logger logger;
    protected final Map<String, Object> parameters;

    protected ProtoObject(ProtoObject parent, Object name, Map<String, Object> map) {
        this.parent = parent;
        this.name = MapUtil.name(name);
        this.onInit = Utils.cast(f(map, "onInit", Consumer.class, v -> {}));
        this.parameters = Collections.unmodifiableMap(new HashMap<>(MapUtil.parameters(map)));
        final Logging logging = f(map, "logging", Logging.class, Logging.DEFAULT);
        this.logger = logging.logger(this);
        this.delegateLogging = logging.delegateLogging(this);
        if (Boolean.TRUE.equals(getDelegateLogging())) {
            addEventListener(event -> event.log(event.getSource()));
        }
    }

    protected Boolean getDelegateLogging() {
        return delegateLogging == null
                ? getParent() != null ? getParent().getDelegateLogging() : null
                : delegateLogging;
    }

    @Override
    public Logger logger() {
        return logger;
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public abstract void start();

    public abstract void stop();

    public void restart() {
        synchronized (this) {
            stop();
        }
        ConcurrentUtils.await(this::isRunning);
        synchronized (this) {
            if (isRunning()) {
                return;
            }
            start();
        }
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

    public void addEventListener(ProtoEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(ProtoEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    protected void fireEvent(ProtoEvent event) {
        for (final ProtoEventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception x) {
                warning("Unable to fire {0}", x, event);
            }
        }
    }

    @Override
    public String toString() {
        return String.join("/", getPath());
    }

    protected interface Logging {

        Logging DEFAULT = new Logging() {};

        default Logger logger(ProtoObject object) {
            return Logger.getLogger(object.toString());
        }

        default Boolean delegateLogging(ProtoObject object) {
            return null;
        }
    }
}
