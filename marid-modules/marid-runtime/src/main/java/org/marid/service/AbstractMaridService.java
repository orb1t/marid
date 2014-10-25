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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMaridService implements MaridService {

    protected final int threadStackSize;
    protected final ThreadGroup threadGroup = new ThreadGroup(getName());
    public final ThreadGroup threadPoolGroup = new ThreadGroup(threadGroup, "pool");
    protected final ThreadPoolExecutor executor;
    protected final boolean poolDaemons;
    protected final boolean daemons;
    protected final long timeGranularity;
    protected final long shutdownTimeout;

    public AbstractMaridService() {
        final ServiceParameters parameters = getClass().getAnnotation(ServiceParameters.class);
        threadStackSize = parameters.stackSize();
        daemons = parameters.daemons();
        poolDaemons = parameters.poolDaemons();
        timeGranularity = parameters.timeUnit().toMillis(parameters.timeGranularity());
        shutdownTimeout = parameters.timeUnit().toMillis(parameters.shutdownTimeout());
        executor = new ThreadPoolExecutor(
                parameters.threads(),
                parameters.maxThreads(),
                parameters.keepAliveTime(),
                parameters.timeUnit(),
                parameters.queueType().apply(parameters.queueSize()),
                poolThreadFactory(),
                parameters.rejectionType().get());
    }

    protected ThreadFactory poolThreadFactory() {
        return this;
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    @Override
    public String toString() {
        return getName();
    }
}
