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

import org.marid.service.data.Request;
import org.marid.service.data.Response;
import org.marid.service.xml.ServiceDescriptor;
import org.marid.typecast.ConfigurableObject;

import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
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

    protected abstract <T extends Response> Future<T> doSend(Request<T> message);

    @Override
    public synchronized final <T extends Response> Future<T> send(final Request<T> message) {
        if (!running()) {
            return ltFuture(new Callable<T>() {
                @Override
                public T call() throws Exception {
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

    protected abstract Transaction doTransaction(Map<String, Object> params);

    private <T> Future<T> ltFuture(Callable<T> task) {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, queue);
        return executor.submit(task);
    }

    @Override
    public synchronized final Transaction transaction(final Map<String, Object> params) {
        class DelayedTransaction implements Transaction {

            private final IdentityHashMap<Request, Future> rfm = new IdentityHashMap<>();
            private final LinkedList<Request> requests = new LinkedList<>();
            private Transaction delegate;

            @Override
            public Service getService() {
                return AbstractService.this;
            }

            @Override
            public <T extends Response> Future<T> submit(final Request<T> request) {
                requests.add(request);
                return ltFuture(new Callable<T>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public T call() throws Exception {
                        synchronized (AbstractService.this) {
                            AbstractService.this.wait();
                        }
                        synchronized (DelayedTransaction.this) {
                            if (delegate == null) {
                                DelayedTransaction.this.wait();
                            }
                        }
                        return (T) rfm.get(request).get();
                    }
                });
            }

            @Override
            public Future<TransactionResult> send() {
                return ltFuture(new Callable<TransactionResult>() {
                    @Override
                    public TransactionResult call() throws Exception {
                        synchronized (AbstractService.this) {
                            AbstractService.this.wait();
                        }
                        synchronized (DelayedTransaction.this) {
                            Transaction d = doTransaction(params);
                            for (Request<?> request : requests) {
                                rfm.put(request, delegate.submit(request));
                            }
                            delegate = d;
                            DelayedTransaction.this.notifyAll();
                        }
                        return delegate.send().get();
                    }
                });
            }
        }
        return new DelayedTransaction();
    }

    @Override
    public Service getService(String type) {
        Service service = Services.getServiceFor(type, descriptor);
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
