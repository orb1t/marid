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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.marid.util.CMPrp;

/**
 * Abstract service.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractService extends CMPrp implements Service {

    protected Thread thread;
    protected ThreadGroup group;

    @Override
    public String name() {
        return $.getString(get("name"), getClass().getSimpleName());
    }

    @Override
    public void init(Map<String, Object> conf) throws Exception {
        put(conf);
        group = new ThreadGroup(SRV_THREAD_GROUP, name());
    }

    @Override
    public synchronized void start() {
        if (running()) {
            throw new IllegalStateException("Already running");
        }
        thread = new Thread(SRV_THREAD_GROUP, this, name());
        thread.start();
    }

    @Override
    public synchronized void stop() {
        if (!running()) {
            throw new IllegalStateException("Is not running");
        }
        for (Thread th : threads()) {
            if (th.isAlive() && !th.isInterrupted()) {
                th.interrupt();
            }
        }
        thread.interrupt();
        thread = null;
    }

    @Override
    public synchronized boolean running() {
        return thread != null && thread.isAlive();
    }

    @Override
    public synchronized Thread addThread(Runnable task) {
        if (!running()) {
            throw new IllegalStateException("Service is not running");
        }
        if (!group.parentOf(Thread.currentThread().getThreadGroup())) {
            throw new IllegalStateException("Not in service thread group");
        }
        return new Thread(group, task);
    }

    @Override
    public synchronized Thread addThread(String name, Runnable task) {
        if (!running()) {
            throw new IllegalStateException("Service is not running");
        }
        if (!group.parentOf(Thread.currentThread().getThreadGroup())) {
            throw new IllegalStateException("Not in service thread group");
        }
        return new Thread(group, task, name);
    }

    @Override
    public void join(long timeout, TimeUnit unit) throws InterruptedException {
        long to = TimeUnit.MILLISECONDS.convert(timeout, unit);
        LinkedList<Thread> thl = new LinkedList<>(Arrays.asList(threads()));
        long t = 0L;
        while (t <= to) {
            Iterator<Thread> i = thl.iterator();
            while (i.hasNext()) {
                if (t > to) {
                    break;
                }
                Thread th = i.next();
                if (!th.isAlive()) {
                    i.remove();
                    continue;
                }
                th.join(1L);
                t++;
            }
        }
    }

    @Override
    public void join() throws InterruptedException {
        for (Thread th : threads()) {
            th.join();
        }
    }

    @Override
    public synchronized Thread[] threads() {
        Thread[] threads = new Thread[group.activeCount()];
        return Arrays.copyOf(threads, group.enumerate(threads));
    }

    @Override
    public synchronized void shutdown() {
        if (group != null) {
            for (Thread th : threads()) {
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
    @SuppressWarnings({
        "FinalizeDeclaration", "FinalizeDoesntCallSuperFinalize"
    })
    protected void finalize() throws Throwable {
        shutdown();
    }
}
