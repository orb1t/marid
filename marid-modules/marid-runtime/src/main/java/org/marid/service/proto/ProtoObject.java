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
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.marid.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ProtoObject<O extends ProtoObject<O>> implements Named, LogSupport, AutoCloseable {

    protected final List<ProtoEventListener> eventListeners = new CopyOnWriteArrayList<>();
    protected final ProtoObject parent;
    protected final String name;
    protected final Consumer<O> onInit;
    protected final Logger logger;
    protected final boolean loggingEnabled;

    public final Map<String, Object> parameters;
    public final ConcurrentMap<String, Object> vars;

    protected ProtoObject(ProtoObject parent, String name, Descriptor<O> descriptor) {
        this.parent = parent;
        this.name = name;
        this.onInit = descriptor::onInit;
        this.parameters = Collections.unmodifiableMap(descriptor.parameters());
        this.vars = new ConcurrentHashMap<>(descriptor.vars());
        this.logger = descriptor.logger(this);
        this.loggingEnabled = descriptor.loggingEnabled();
    }

    @Override
    public Logger logger() {
        return logger;
    }

    protected void init() {
        onInit.accept(Utils.cast(this));
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

    public void restart() {
        stop();
        ConcurrentUtils.await(() -> !isRunning());
        start();
    }

    public abstract boolean isRunning();

    public abstract boolean isStarted();

    public abstract boolean isStopped();

    public abstract ProtoObject getAt(String name);

    public List<String> getPath() {
        final List<String> path = new ArrayList<>();
        for (ProtoObject o = this; o != null; o = o.getParent()) {
            path.add(0, o.getName());
        }
        return path;
    }

    public abstract ProtoObject getContext();

    public void addEventListener(ProtoEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(ProtoEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    protected void fireEvent(Level level, String message, Throwable cause, Object... args) {
        final ProtoEvent event = new ProtoEvent(this, level, message, cause, args);
        for (final ProtoEventListener listener : eventListeners) {
            try {
                listener.onEvent(event);
            } catch (Exception x) {
                log(WARNING, "Unable to fire {0}", x, event);
            }
        }
        if (loggingEnabled) {
            Log.log(logger, level, message, cause, args);
        }
    }

    protected void fireEvent(String message, Object... args) {
        fireEvent(Level.INFO, message, null, args);
    }

    protected void fireEvent(String message, Throwable cause, Object... args) {
        fireEvent(Level.WARNING, message, cause, args);
    }

    protected void fireData(Object... args) {
        fireEvent(Level.OFF, "data", null, args);
    }

    @Override
    public String toString() {
        return String.join("/", getPath());
    }

    public interface Descriptor<O extends ProtoObject<O>> {

        default void onInit(O object) {
        }

        default Map<String, Object> parameters() {
            return Collections.emptyMap();
        }

        default Map<String, Object> vars() {
            return Collections.emptyMap();
        }

        default Logger logger(ProtoObject object) {
            return Logger.getLogger(object.toString());
        }

        default boolean loggingEnabled() {
            return true;
        }
    }
}
