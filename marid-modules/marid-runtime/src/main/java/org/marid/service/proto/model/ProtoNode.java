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

package org.marid.service.proto.model;

import org.marid.service.proto.util.EmptyScheduledFuture;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoNode extends AbstractProtoObject implements ProtoTaskSupport {

    protected final AbstractProtoObject parent;

    protected ScheduledFuture<?> task;

    public ProtoNode(@Nonnull ProtoBus bus, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        this((AbstractProtoObject) bus, name, map);
    }

    public ProtoNode(@Nonnull ProtoNode node, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        this((AbstractProtoObject) node, name, map);
    }

    private ProtoNode(@Nonnull AbstractProtoObject object, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        super(name, object.getVariables(), map);
        parent = object;
        ProtoTaskSupport.putProperties(map, this);
        putProperty(map, "period", long.class);
        putProperty(map, "realTime", boolean.class);
        putProperty(map, "delay", long.class);
        putProperty(map, "task", Consumer.class);
        putProperty(map, "busTimer", boolean.class);
    }

    public Consumer<ProtoNode> getTaskConsumer() {
        return getProperty("task", () -> node -> {});
    }

    public long getPeriod() {
        return getProperty("period", () -> 0L);
    }

    public boolean isRealTime() {
        return getProperty("realTime", () -> false);
    }

    public long getDelay() {
        return getProperty("delay", () -> 0L);
    }

    public boolean isBusTimer() {
        return getProperty("busTimer", () -> true);
    }

    @Nonnull
    public ProtoBus getBus() {
        for (AbstractProtoObject object = parent; object != null; object = object.getParent()) {
            if (object instanceof ProtoBus) {
                return (ProtoBus) object;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public AbstractProtoObject getParent() {
        return parent;
    }

    @Override
    public ProtoContext getContext() {
        return getBus().getContext();
    }

    @Override
    public synchronized void start() {
        if (task == null) {
            final long period = getPeriod();
            final long delay = getDelay();
            final boolean realTime = isRealTime();
            final Consumer<ProtoNode> taskConsumer = getTaskConsumer();
            final ScheduledThreadPoolExecutor timer = isBusTimer() ? getBus().timer : getContext().timer;
            final Runnable t = () -> taskConsumer.accept(this);
            if (period > 0L) {
                if (realTime) {
                    task = timer.scheduleAtFixedRate(t, delay, period, TimeUnit.SECONDS);
                } else {
                    task = timer.scheduleWithFixedDelay(t, delay, period, TimeUnit.SECONDS);
                }
            } else {
                if (delay > 0L) {
                    task = timer.schedule(t, delay, TimeUnit.SECONDS);
                } else {
                    task = EmptyScheduledFuture.getInstance();
                }
            }
        }
    }

    @Override
    public synchronized void stop() {
        if (task != null) {
            if (!task.isDone()) {
                task.cancel(isInterruptTask());
            }
            try {
                while (!task.isDone()) {
                    LockSupport.parkNanos(1L);
                }
            } finally {
                task = null;
            }
        }
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
    public void close() {
        stop();
    }
}
