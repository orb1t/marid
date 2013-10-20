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

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.MoreExecutors;
import groovy.lang.Closure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.logging.Logger;

import static org.marid.groovy.GroovyRuntime.get;
import static org.marid.l10n.Localized.S;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMaridService extends AbstractService implements MaridService {

    protected final int threadStackSize;
    protected final String type;
    protected final String id;
    protected final ThreadGroup threadGroup;
    protected final ThreadGroup threadPoolGroup;
    protected final ThreadPoolExecutor executor;
    protected final boolean poolDaemons;
    protected final boolean daemons;
    protected final long timeGranularity;
    protected final long shutdownTimeout;
    protected final Logger log = Logger.getLogger(getClass().getName());

    public AbstractMaridService(Map<String, Object> params) {
        threadStackSize = get(int.class, params, "threadStackSize", 0);
        type = get(String.class, params, "type", defaultType());
        id = get(String.class, params, "id", type);
        daemons = get(boolean.class, params, "daemons", false);
        poolDaemons = get(boolean.class, params, "poolDaemons", false);
        timeGranularity = get(long.class, params, "timeGranularity", 1000L);
        shutdownTimeout = get(long.class, params, "shutdownTimeout", 60_000L);
        threadGroup = new ThreadGroup(id);
        threadPoolGroup = new ThreadGroup(threadGroup, id + ".pool");
        executor = new ThreadPoolExecutor(
                get(int.class, params, "threadPoolInitSize", 0),
                get(int.class, params, "threadPoolMaxSize", 1),
                get(long.class, params, "threadPoolKeepAliveTime", 60_000L),
                TimeUnit.MILLISECONDS,
                getBlockingQueue(params),
                new ThreadFactory() {
                    private short num;

                    @SuppressWarnings("NullableProblems")
                    @Override
                    public Thread newThread(Runnable r) {
                        final String name = "thread-" + num++;
                        final Thread thread = new Thread(threadPoolGroup, r, name, threadStackSize);
                        thread.setDaemon(poolDaemons);
                        return thread;
                    }
                },
                getRejectedExecutionHandler(params));
        fine(log, "{0} Params {1}", id + ":" + type, params);
        addListener(new Listener() {
            @Override
            public void failed(State from, Throwable failure) {
                warning(log, "{0} Failed from {1}", failure, AbstractMaridService.this, from);
            }

            @Override
            public void running() {
                info(log, "{0} Running", AbstractMaridService.this);
            }

            @Override
            public void starting() {
                info(log, "{0} Starting", AbstractMaridService.this);
            }

            @Override
            public void stopping(State from) {
                fine(log, "{0} Stopping", AbstractMaridService.this);
            }

            @Override
            public void terminated(State from) {
                info(log, "{0} Terminated", AbstractMaridService.this);
                executor.shutdown();
                try {
                    if (executor.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                } catch (InterruptedException x) {
                    warning(log, "{0} Interrupted", x, AbstractMaridService.this);
                }
                final List<Runnable> rest = executor.shutdownNow();
                severe(log, "{0} Shutdown error. Skipped {1} tasks", AbstractMaridService.this, rest.size());
            }
        }, MoreExecutors.sameThreadExecutor());
    }

    private String defaultType() {
        final String[] cps = getClass().getCanonicalName().split("[.]");
        return cps.length > 1 ? cps[cps.length - 2] : cps[0];
    }

    @SuppressWarnings("unchecked")
    private BlockingQueue<Runnable> getBlockingQueue(Map<String, Object> params) {
        final Object value = params.get("blockingQueue");
        if (value == null) {
            return new SynchronousQueue<>();
        } else if (value instanceof String) {
            switch ((String) value) {
                case "linked":
                    return new LinkedBlockingQueue<>();
                case "array":
                    return new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors() * 2);
                case "synchronous":
                    return new SynchronousQueue<>();
                default:
                    throw new IllegalArgumentException("Invalid blocking queue type: " + value);
            }
        } else if (value instanceof Supplier) {
            return ((Supplier<BlockingQueue<Runnable>>) value).get();
        } else if (value instanceof BlockingQueue) {
            return (BlockingQueue<Runnable>) value;
        } else if (value instanceof Number) {
            return new ArrayBlockingQueue<>(((Number) value).intValue());
        } else if (value instanceof Boolean) {
            return new SynchronousQueue<>((Boolean) value);
        } else if (value instanceof Closure) {
            return ((Closure<BlockingQueue<Runnable>>) value).call(params);
        } else {
            throw new IllegalArgumentException("Illegal value for blockingQueue: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private RejectedExecutionHandler getRejectedExecutionHandler(Map<String, Object> params) {
        final Object value = params.get("rejectedExecutionHandler");
        if (value == null) {
            return new CallerRunsPolicy();
        } else if (value instanceof String) {
            switch ((String) value) {
                case "callerRunsPolicy":
                    return new CallerRunsPolicy();
                case "discardPolicy":
                    return new DiscardPolicy();
                case "discardOldestPolicy":
                    return new DiscardOldestPolicy();
                case "abortPolicy":
                    return new AbortPolicy();
                default:
                    throw new IllegalArgumentException("Invalid rejection handler: " + value);
            }
        } else if (value instanceof Supplier) {
            return ((Supplier<RejectedExecutionHandler>) value).get();
        } else if (value instanceof RejectedExecutionException) {
            return (RejectedExecutionHandler) value;
        } else if (value instanceof Closure) {
            return ((Closure<RejectedExecutionHandler>) value).call(params);
        } else {
            throw new IllegalArgumentException("Illegal value for rejectionExecutionHandler: " + value);
        }
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(threadGroup, r, String.valueOf(r), threadStackSize);
        thread.setDaemon(daemons);
        return thread;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return S.l(getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return id() + ":" + type();
    }

    protected abstract Object processMessage(Object message) throws Exception;

    @Override
    public <T> Future<T> send(final Object message) {
        return executor.submit(new Callable<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T call() throws Exception {
                while (!isRunning()) {
                    try {
                        awaitRunning(timeGranularity, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException x) {
                        if (Thread.interrupted()) {
                            warning(log, "{0} Interrupted {1}", AbstractMaridService.this, this);
                            throw new InterruptedException();
                        }
                    }
                }
                return (T) processMessage(message);
            }
        });
    }
}
