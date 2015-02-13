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

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMaridService implements MaridService {

    protected final AtomicInteger threadCounter = new AtomicInteger();
    protected final int threadStackSize;
    protected final ThreadGroup threadGroup;
    protected final ThreadGroup threadPoolGroup;
    protected final ExecutorService executor;
    protected final boolean poolDaemons;
    protected final boolean daemons;
    protected final long shutdownTimeout;
    protected final String name;

    public AbstractMaridService(MaridServiceConfig conf) {
        name = conf.name();
        threadStackSize = conf.stackSize();
        threadGroup = new ThreadGroup(getName());
        threadPoolGroup = new ThreadGroup(threadGroup, "pool");
        daemons = conf.daemons();
        poolDaemons = conf.poolDaemons();
        shutdownTimeout = conf.timeUnit().toMillis(conf.shutdownTimeout());
        executor = executorService(conf);
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
    }

    @Override
    public boolean isRunning() {
        return !executor.isShutdown();
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        executor.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread t = new Thread(threadGroup, r, "t-" + threadCounter.getAndIncrement(), threadStackSize);
        t.setDaemon(daemons);
        return t;
    }

    protected ThreadFactory threadFactory(MaridServiceConfig conf) {
        return r -> {
            final Thread t = new Thread(threadPoolGroup, r, "w-" + threadCounter.getAndIncrement(), conf.stackSize());
            t.setDaemon(conf.poolDaemons());
            return t;
        };
    }

    protected ExecutorService executorService(MaridServiceConfig conf) {
        return new ThreadPoolExecutor(
                conf.threads(),
                conf.maxThreads(),
                conf.keepAliveTime(),
                conf.timeUnit(),
                conf.blockingQueue(),
                threadFactory(conf),
                conf.rejectedExecutionHandler());
    }

    @Override
    public String toString() {
        return getName();
    }
}
