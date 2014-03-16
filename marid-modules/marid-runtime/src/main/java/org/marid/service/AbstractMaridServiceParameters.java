/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractMaridServiceParameters implements MaridServiceParameters {

    int threadStackSize = 0;
    boolean daemons = false;
    boolean poolDaemons = false;
    long timeGranularity = TimeUnit.SECONDS.toMillis(1L);
    long shutdownTimeout = TimeUnit.MINUTES.toMillis(1L);
    int threadPoolInitSize = 0;
    int threadPoolMaxSize = 1;
    long threadPoolKeepAliveTime = TimeUnit.MINUTES.toMillis(1L);
    Supplier<BlockingQueue<Runnable>> blockingQueueSupplier = SynchronousQueue::new;
    Function<AbstractMaridService, ThreadFactory> poolThreadFactory = s -> r -> new Thread(s.threadPoolGroup, r, r.toString(), threadStackSize);
    Function<AbstractMaridService, RejectedExecutionHandler> rejectedExecutionHandler = s -> new CallerRunsPolicy();

    public int getThreadStackSize() {
        return threadStackSize;
    }

    public void setThreadStackSize(int threadStackSize) {
        this.threadStackSize = threadStackSize;
    }

    public boolean isDaemons() {
        return daemons;
    }

    public void setDaemons(boolean daemons) {
        this.daemons = daemons;
    }

    public boolean isPoolDaemons() {
        return poolDaemons;
    }

    public void setPoolDaemons(boolean poolDaemons) {
        this.poolDaemons = poolDaemons;
    }

    public long getTimeGranularity() {
        return timeGranularity;
    }

    public void setTimeGranularity(long timeGranularity) {
        this.timeGranularity = timeGranularity;
    }

    public long getShutdownTimeout() {
        return shutdownTimeout;
    }

    public void setShutdownTimeout(long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public int getThreadPoolInitSize() {
        return threadPoolInitSize;
    }

    public void setThreadPoolInitSize(int threadPoolInitSize) {
        this.threadPoolInitSize = threadPoolInitSize;
    }

    public long getThreadPoolKeepAliveTime() {
        return threadPoolKeepAliveTime;
    }

    public void setThreadPoolKeepAliveTime(long threadPoolKeepAliveTime) {
        this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
    }

    public int getThreadPoolMaxSize() {
        return threadPoolMaxSize;
    }

    public void setThreadPoolMaxSize(int threadPoolMaxSize) {
        this.threadPoolMaxSize = threadPoolMaxSize;
    }

    public Supplier<BlockingQueue<Runnable>> getBlockingQueueSupplier() {
        return blockingQueueSupplier;
    }

    public void setBlockingQueueSupplier(Supplier<BlockingQueue<Runnable>> blockingQueueSupplier) {
        this.blockingQueueSupplier = blockingQueueSupplier;
    }

    public Function<AbstractMaridService, ThreadFactory> getPoolThreadFactory() {
        return poolThreadFactory;
    }

    public void setPoolThreadFactory(Function<AbstractMaridService, ThreadFactory> poolThreadFactory) {
        this.poolThreadFactory = poolThreadFactory;
    }

    public Function<AbstractMaridService, RejectedExecutionHandler> getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(Function<AbstractMaridService, RejectedExecutionHandler> rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }
}
