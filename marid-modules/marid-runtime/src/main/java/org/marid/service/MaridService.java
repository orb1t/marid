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

import org.marid.logging.LogSupport;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

import static org.marid.service.MaridServices.SERVICES;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridService extends LogSupport, ThreadFactory, AutoCloseable, MaridServiceMXBean {

    ThreadGroup threadGroup();

    default void start() throws Exception {
        SERVICES.computeIfAbsent(getClass(), c -> new ConcurrentLinkedQueue<>()).add(this);
    }

    @Override
    default void close() throws Exception {
    }

    @Override
    default boolean isRunning() {
        return threadGroup().activeCount() > 0;
    }

    @Override
    default int getThreadCount() {
        return threadGroup().activeCount();
    }

    @Override
    default boolean isDaemons() {
        return false;
    }

    @Override
    default Thread newThread(Runnable r) {
        final Thread thread = new Thread(threadGroup(), r, String.valueOf(r), getStackSize());
        thread.setDaemon(isDaemons());
        return thread;
    }

    @Override
    default int getStackSize() {
        return 0;
    }

    @Override
    default String getName() {
        return getClass().getSimpleName();
    }
}
