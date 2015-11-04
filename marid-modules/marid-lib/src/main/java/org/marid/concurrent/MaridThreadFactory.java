/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import static java.lang.Character.MAX_RADIX;
import static java.lang.System.identityHashCode;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridThreadFactory implements ThreadFactory, Consumer<Runnable> {

    private final ThreadGroup group;
    private final String name;
    private final boolean daemon;
    private final long stackSize;

    public MaridThreadFactory(ThreadGroup group, String name, boolean daemon, long stackSize) {
        this.group = group;
        this.name = name;
        this.daemon = daemon;
        this.stackSize = stackSize;
    }

    public MaridThreadFactory(String name, boolean daemon, long stackSize) {
        this(Thread.currentThread().getThreadGroup(), name, daemon, stackSize);
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Thread thread = new Thread(group, r, name + Integer.toString(identityHashCode(r), MAX_RADIX), stackSize);
        thread.setDaemon(daemon);
        return thread;
    }

    @Override
    public void accept(Runnable runnable) {
        newThread(runnable).start();
    }
}
