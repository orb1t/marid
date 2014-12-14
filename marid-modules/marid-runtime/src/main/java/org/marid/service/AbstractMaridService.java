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

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMaridService implements MaridService {

    protected final int threadStackSize;
    protected final ThreadGroup threadGroup;
    protected final ThreadGroup threadPoolGroup;
    protected final ThreadPoolExecutor executor;
    protected final boolean poolDaemons;
    protected final boolean daemons;
    protected final long timeGranularity;
    protected final long shutdownTimeout;
    protected final String name;

    public AbstractMaridService(MaridServiceConfig conf) {
        name = conf.name();
        threadStackSize = conf.stackSize();
        threadGroup = new ThreadGroup(getName());
        threadPoolGroup = new ThreadGroup(threadGroup, "pool");
        daemons = conf.daemons();
        poolDaemons = conf.poolDaemons();
        timeGranularity = conf.timeUnit().toMillis(conf.timeGranularity());
        shutdownTimeout = conf.timeUnit().toMillis(conf.shutdownTimeout());
        executor = new ThreadPoolExecutor(
                conf.threads(),
                conf.maxThreads(),
                conf.keepAliveTime(),
                conf.timeUnit(),
                conf.queueType().apply(conf.queueSize()),
                poolThreadFactory(),
                conf.rejectionType().get());
    }

    protected ThreadFactory poolThreadFactory() {
        return this;
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    public ThreadGroup getThreadPoolGroup() {
        return threadPoolGroup;
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
    public String toString() {
        return getName();
    }
}
