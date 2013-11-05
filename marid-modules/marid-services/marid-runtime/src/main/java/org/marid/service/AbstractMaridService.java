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

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.MoreExecutors;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.marid.l10n.Localized.S;
import static org.marid.methods.LogMethods.*;
import static org.marid.methods.PropMethods.*;
import static java.util.Collections.unmodifiableMap;

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
    protected final Executor sameThreadExecutor = MoreExecutors.sameThreadExecutor();
    protected final MetaClass metaClass = InvokerHelper.getMetaClass(getClass());
    protected final Logger log = Logger.getLogger(getClass().getName());
    protected final Map<String, ServiceMethodInfo> methodMap = new HashMap<>();

    public AbstractMaridService(Map params) {
        threadStackSize = get(params, int.class, "threadStackSize", 0);
        type = get(params, String.class, "type", defaultType());
        id = get(params, String.class, "id", type);
        daemons = get(params, boolean.class, "daemons", false);
        poolDaemons = get(params, boolean.class, "poolDaemons", false);
        timeGranularity = get(params, long.class, "timeGranularity", 1000L);
        shutdownTimeout = get(params, long.class, "shutdownTimeout", 60_000L);
        threadGroup = new ThreadGroup(id);
        threadPoolGroup = new ThreadGroup(threadGroup, id + ".pool");
        executor = new ThreadPoolExecutor(
                get(params, int.class, "threadPoolInitSize", 0),
                get(params, int.class, "threadPoolMaxSize", 1),
                get(params, long.class, "threadPoolKeepAliveTime", 60_000L),
                TimeUnit.MILLISECONDS,
                getBlockingQueue(params, "blockingQueue", 0),
                getThreadFactory(params, "threadPoolThreadFactory", threadPoolGroup, poolDaemons, threadStackSize),
                getRejectedExecutionHandler(params, "rejectedExecutionHandler"));
        fine(log, "{0} Params {1}", id + ":" + type, params);
        addListener(new Listener() {
            @Override
            public void failed(State from, Throwable failure) {
                warning(log, "{0} Failed from {1}", failure, AbstractMaridService.this, from);
            }

            @Override
            public void running() {
                info(log, "{0} Running", AbstractMaridService.this);
                onRunning();
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
                onTerminated();
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
        }, sameThreadExecutor);
        for (final Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(ServiceMethod.class)) {
                final ServiceMethod serviceMethod = method.getAnnotation(ServiceMethod.class);
                methodMap.put(method.getName(), new ServiceMethodInfo(serviceMethod.group()));
            }
        }
    }

    protected void onRunning() {
    }

    protected void onTerminated() {
    }

    private String defaultType() {
        final String[] cps = getClass().getCanonicalName().split("[.]");
        return cps.length > 1 ? cps[cps.length - 2] : cps[0];
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
    public Map<String, ServiceMethodInfo> methodMap() {
        return unmodifiableMap(methodMap);
    }

    @Override
    public String toString() {
        return id() + ":" + type();
    }

    @Override
    public <T> Future<T> send(final String method, final Object... args) {
        if (!methodMap.containsKey(method)) {
            throw new SecurityException("Invalid method '" + method + "'");
        }
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
                return (T) metaClass.invokeMethod(AbstractMaridService.this, method, args);
            }
        });
    }
}
