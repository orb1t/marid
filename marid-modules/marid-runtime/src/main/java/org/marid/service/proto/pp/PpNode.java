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

package org.marid.service.proto.pp;

import org.marid.functions.SafeConsumer;
import org.marid.service.proto.ProtoObject;
import org.marid.service.util.EmptyScheduledFuture;

import javax.annotation.Nonnull;
import java.io.InterruptedIOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public class PpNode extends ProtoObject<PpNode> {

    protected final TreeMap<String, PpNode> nodeMap = new TreeMap<>();
    protected final ToLongFunction<PpNode> period;
    protected final ToLongFunction<PpNode> delay;
    protected final Predicate<PpNode> realTime;
    protected final Predicate<PpNode> interruptTask;
    protected final SafeConsumer<PpNode> callable;

    protected ScheduledFuture<?> task;

    protected PpNode(@Nonnull ProtoObject object, @Nonnull String name, @Nonnull Descriptor descriptor) {
        super(object, name, descriptor);
        period = descriptor::period;
        delay = descriptor::delay;
        realTime = descriptor::realTime;
        callable = descriptor::task;
        interruptTask = descriptor::interruptTask;
        descriptor.nodes().forEach((k, v) -> nodeMap.put(k, new PpNode(this, k, v)));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Nonnull
    public PpBus getBus() {
        for (ProtoObject object = parent; object != null; object = object.getParent()) {
            if (object instanceof PpBus) {
                return (PpBus) object;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public PpContext getContext() {
        return getBus().getContext();
    }

    @Override
    public synchronized void start() {
        if (task == null) {
            try {
                final Runnable t = () -> {
                    try {
                        callable.acceptUnsafe(this);
                    } catch (ClosedChannelException x) {
                        fireEvent("io {0}", "closed", x);
                    } catch (InterruptedIOException x) {
                        fireEvent("io {0}", "interrupted", x);
                    } catch (Exception x) {
                        fireEvent("task", x);
                    }
                };
                final long period = this.period.applyAsLong(this);
                final long delay = this.delay.applyAsLong(this);
                if (period > 0L) {
                    if (this.realTime.test(this)) {
                        this.task = getContext().timer.scheduleAtFixedRate(t, delay, period, TimeUnit.SECONDS);
                    } else {
                        this.task = getContext().timer.scheduleWithFixedDelay(t, delay, period, TimeUnit.SECONDS);
                    }
                } else {
                    if (delay > 0L) {
                        this.task = getContext().timer.schedule(t, delay, TimeUnit.SECONDS);
                    } else {
                        this.task = EmptyScheduledFuture.getInstance();
                    }
                }
            } catch (Exception x) {
                fireEvent("start", x);
            }
            fireEvent("start");
        }
    }

    @Override
    public synchronized void stop() {
        if (task != null) {
            if (!task.isDone()) {
                task.cancel(interruptTask.test(this));
            }
            try {
                while (!task.isDone()) {
                    LockSupport.parkNanos(1L);
                }
            } finally {
                task = null;
            }
        }
        fireEvent("stop");
    }

    @Override
    public synchronized boolean isRunning() {
        return task != null && !task.isDone();
    }

    @Override
    public synchronized boolean isStarted() {
        return task != null;
    }

    @Override
    public synchronized boolean isStopped() {
        return task == null;
    }

    @Override
    public PpNode getChild(String name) {
        return nodeMap.get(name);
    }

    @Override
    public void close() {
        stop();
        fireEvent("close");
    }

    public interface Descriptor extends ProtoObject.Descriptor<PpNode> {

        default boolean interruptTask(PpNode node) {
            return true;
        }

        default long period(PpNode node) {
            return 0L;
        }

        default long delay(PpNode node) {
            return 0L;
        }

        default boolean realTime(PpNode node) {
            return false;
        }

        default void task(PpNode node) throws Exception {
        }

        default Map<String, PpNode.Descriptor> nodes() {
            return Collections.emptyMap();
        }
    }
}
