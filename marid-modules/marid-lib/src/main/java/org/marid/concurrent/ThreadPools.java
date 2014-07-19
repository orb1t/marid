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

package org.marid.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ThreadPools {

    public static int getPoolSize(int minPoolSize) {
        return Math.max(minPoolSize, Runtime.getRuntime().availableProcessors());
    }

    public static ThreadPoolExecutor newThreadPool(int threads, long keepAliveTime, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(0, threads, keepAliveTime, MILLISECONDS, new SynchronousQueue<>(), handler);
    }

    public static ThreadPoolExecutor newThreadPool(int threads, long keepAliveTime) {
        return newThreadPool(threads, keepAliveTime, new CallerRunsPolicy());
    }

    public static ThreadPoolExecutor newArrayThreadPool(int threads, int size, RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(threads, threads, 0L, MILLISECONDS, new ArrayBlockingQueue<>(size), handler);
    }
}
