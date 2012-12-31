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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.marid.util.ConcurrentMutablePropertized;

/**
 * Abstract service.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractService extends ConcurrentMutablePropertized implements Service {

    protected Thread thread;
    protected List<Thread> threads;
    protected ThreadGroup group;
    protected Thread monitor;

    @Override
    public String getName() {
        return $.getString(get("name"), getClass().getSimpleName());
    }

    @Override
    public void init(Map<String, Object> conf) throws Exception {
        put(conf);
        threads = new LinkedList<>();
        group = new ThreadGroup(SRV_THREAD_GROUP, getName());
        monitor = new Thread(new Runnable() {
            @Override
            @SuppressWarnings("SleepWhileInLoop")
            public void run() {
                while (true) try {
                    long period = $.getLong(get("threadMonitorPeriod"), 1000L);
                    Thread.sleep(period);
                    synchronized(AbstractService.this) {
                        if (threads.isEmpty()) {
                            continue;
                        }
                        Iterator<Thread> i = threads.iterator();
                        while (i.hasNext()) {
                            Thread th = i.next();
                            if (!th.isAlive() || th.isInterrupted()) {
                                i.remove();
                            }
                        }
                    }
                } catch (InterruptedException x) {
                    break;
                }
                threads.clear();
                threads = null;
            }
        }, getName() + "-monitor");
        monitor.setDaemon(true);
        monitor.start();
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
        Thread th = new Thread(group, task);
        threads.add(th);
        return th;
    }

    @Override
    public synchronized Thread addThread(String name, Runnable task) {
        Thread th = new Thread(group, task, name);
        threads.add(th);
        return th;
    }

    @Override
    public synchronized void shutdown() {
        if (thread != null) {
            if (!thread.isInterrupted()) {
                thread.interrupt();
            }
            thread = null;
        }
        if (monitor != null) {
            if (!monitor.isInterrupted()) {
                monitor.interrupt();
            }
            monitor = null;
        }
        group = null;
    }

    @Override
    @SuppressWarnings({
        "FinalizeDeclaration", "FinalizeDoesntCallSuperFinalize"
    })
    protected void finalize() throws Throwable {
        shutdown();
    }
}
