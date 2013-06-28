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

package org.marid.service;

import org.marid.typecast.ConfigurableObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractService extends ConfigurableObject implements Service {

    private final ThreadGroup threadGroup = new ThreadGroup(getClass().getSimpleName());
    private final ServiceDescriptor descriptor;
    private final String id;
    private final String type;
    private boolean running;

    protected final Logger log = Logger.getLogger(getClass().getCanonicalName());

    public AbstractService(String id, String type, ServiceDescriptor descriptor) {
        this.id = id;
        this.type = type;
        this.descriptor = descriptor;
    }

    @Override
    public ServiceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public String id() {
        return id;
    }

    private Package getPackage() {
        Package pkg = getClass().getPackage();
        if (pkg == null) {
            throw new IllegalStateException("Null package for service " + getClass());
        } else {
            return pkg;
        }
    }

    @Override
    public String name() {
        String name = getPackage().getImplementationTitle();
        return name == null ? getClass().getSimpleName() : name;
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String label() {
        return S.l(name());
    }

    @Override
    public String version() {
        String version = getPackage().getImplementationVersion();
        return version == null ? "DEV" : version;
    }

    @Override
    public Thread newThread(Runnable task) {
        return new Thread(threadGroup, task, task.toString(), getInt("threadStackSize", 0));
    }

    @Override
    public synchronized final boolean running() {
        return running;
    }

    protected abstract void doStart() throws Exception;

    @Override
    public synchronized final void start() throws Exception {
        if (!running()) {
            doStart();
            running = true;
            notifyAll();
        }
    }

    protected abstract void doStop() throws Exception;

    @Override
    public synchronized final void stop() throws Exception {
        if (running()) {
            doStop();
            running = false;
        }
    }

    protected abstract Future<?> doSend(Object message);

    protected abstract Future<List<?>> doSend(Object... messages);

    private <T> Future<T> ltFuture(Callable<T> task) {
        return new ThreadPoolExecutor(0, 1, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), this).submit(task);
    }

    @Override
    public synchronized final Future<?> send(final Object message) {
        if (!running()) {
            return ltFuture(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    synchronized (AbstractService.this) {
                        if (!running) {
                            AbstractService.this.wait();
                        }
                        return doSend(message).get();
                    }
                }
            });
        } else {
            return doSend(message);
        }
    }

    public synchronized final Future<List<?>> send(final Object... messages) {
        if (!running()) {
            return ltFuture(new Callable<List<?>>() {
                @Override
                public List<?> call() throws Exception {
                    synchronized (AbstractService.this) {
                        if (!running) {
                            AbstractService.this.wait();
                        }
                        return doSend(messages).get();
                    }
                }
            });
        } else {
            return doSend(messages);
        }
    }

    @Override
    public Service getService(String type) {
        Service service = ServiceMappers.getServiceMapper().getService(type, descriptor);
        if (service == null) {
            throw new NoSuchElementException(toString() + ": no services found for type: " + type);
        } else {
            return service;
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
        return label() + " " + version();
    }
}
