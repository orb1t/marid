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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.marid.util.CMPrp;

/**
 * Abstract service.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractService extends CMPrp implements Service {

    private static final Map<Integer, Logger> lc = new ConcurrentHashMap<>();

    protected Thread thread;
    protected ThreadGroup group;

    @Override
    public String getName() {
        return $.getString(get("name"), getClass().getSimpleName());
    }

    @Override
    public void init(Map<String, Object> conf) throws Exception {
        put(conf);
        group = new ThreadGroup(SRV_THREAD_GROUP, getName());
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            throw new IllegalStateException("Already running");
        }
        thread = new Thread(SRV_THREAD_GROUP, this, getName());
        thread.start();
    }

    @Override
    public synchronized void stop() {
        if (!isRunning()) {
            throw new IllegalStateException("Is not running");
        }
        for (Thread th : getThreads()) {
            if (th.isAlive() && !th.isInterrupted()) {
                th.interrupt();
            }
        }
        thread.interrupt();
        thread = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    @Override
    public synchronized Thread addThread(Runnable task) {
        return new Thread(group, task);
    }

    @Override
    public synchronized Thread addThread(String name, Runnable task) {
        return new Thread(group, task, name);
    }

    @Override
    public synchronized void shutdown() {
        if (group != null) {
            Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);
            for (Thread th : threads) {
                if (!th.isInterrupted()) {
                    th.interrupt();
                }
            }
            group = null;
        }
        if (thread != null) {
            if (!thread.isInterrupted()) {
                thread.interrupt();
            }
            thread = null;
        }
    }

    @Override
    public Logger l() {
        int key = System.identityHashCode(getLog().intern());
        Logger l = lc.get(key);
        if (l == null) {
            lc.put(key, l = Logger.getLogger(getLog(), "res.messages"));
        }
        return l;
    }

    @Override
    @SuppressWarnings({
        "FinalizeDeclaration", "FinalizeDoesntCallSuperFinalize"
    })
    protected void finalize() throws Throwable {
        shutdown();
    }
}
