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

import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface MaridServiceConfig {

    default BlockingQueue<Runnable> blockingQueue() {
        return new SynchronousQueue<>();
    }

    default int threads() {
        return 0;
    }

    default int maxThreads() {
        return 8;
    }

    default long keepAliveTime() {
        return 0L;
    }

    default TimeUnit timeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    default int stackSize() {
        return 0;
    }

    default boolean daemons() {
        return false;
    }

    default boolean poolDaemons() {
        return false;
    }

    default long shutdownTimeout() {
        return 10_000L;
    }

    default RejectedExecutionHandler rejectedExecutionHandler() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    default String name() {
        return UUID.randomUUID().toString();
    }
}
