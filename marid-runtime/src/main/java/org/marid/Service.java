/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.marid.util.Prp;

/**
 * Marid service.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Service extends Runnable, Prp {

    /**
     * Marid services thread group.
     */
    public static final ThreadGroup SRV_THREAD_GROUP = new ThreadGroup("marid");

    /**
     * Initializes the service.
     * @param conf Service configuration.
     * @throws Exception An exception during the initializtion.
     */
    public void init(Map<String, Object> conf) throws Exception;

    /**
     * Get the service thread group.
     * @return Service thread group.
     */
    public ThreadGroup threadGroup();

    /**
     * Adds a thread.
     * @param name Thread name.
     * @param task Runnable task.
     * @return Created thread.
     */
    public Thread addThread(String name, Runnable task);

    /**
     * Adds a thread.
     * @param task Runnable task.
     * @return Created thread.
     */
    public Thread addThread(Runnable task);

    /**
     * Get the current service thread.
     * @return Current service thread.
     */
    public Thread thread();

    /**
     * Get collection of threads.
     * @return Current threads collection.
     */
    public Thread[] threads();

    /**
     * Get the running flag.
     * @return Running flag.
     */
    public boolean running();

    /**
     * Starts the service.
     */
    public void start();

    /**
     * Stops the service.
     */
    public void stop();

    /**
     * Waits until the service will terminated.
     * @param timeout Timeout.
     * @param unit Timeout unit.
     * @throws InterruptedException An interrupted exception.
     */
    public void join(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Waits until the service will terminated.
     * @throws InterruptedException An interrupted exception.
     */
    public void join() throws InterruptedException;

    /**
     * Get the service name.
     * @return Service name.
     */
    public String name();

    /**
     * Shutdowns the service.
     */
    public void shutdown();
}
