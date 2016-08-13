/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.proto;

import org.marid.io.IOBiConsumer;
import org.marid.io.IOBiFunction;
import org.marid.io.IOConsumer;
import org.marid.proto.io.ProtoIO;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.marid.logging.LogSupport.WARNING;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoBusTaskRunner implements ProtoBusTaskRunner<StdProtoBus> {

    private final StdProtoBus bus;

    public StdProtoBusTaskRunner(StdProtoBus bus) {
        this.bus = bus;
    }

    @Override
    public Future<?> runAsync(IOBiConsumer<StdProtoBus, ProtoIO> consumer) {
        return bus.scheduler.submit(() -> run(consumer));
    }

    @Override
    public <R> Future<R> callAsync(IOBiFunction<StdProtoBus, ProtoIO, R> function) {
        return bus.scheduler.submit(() -> call(function));
    }

    @Override
    public ScheduledFuture<?> schedule(IOBiConsumer<StdProtoBus, ProtoIO> task, long delay, long period, TimeUnit unit, boolean fair) {
        if (period == 0) {
            return bus.scheduler.schedule(() -> run(task), delay, unit);
        } else {
            return fair
                    ? bus.scheduler.scheduleAtFixedRate(() -> run(task), delay, period, unit)
                    : bus.scheduler.scheduleWithFixedDelay(() -> run(task), delay, period, unit);
        }
    }

    @Override
    public void run(IOBiConsumer<StdProtoBus, ProtoIO> consumer) {
        doWithChannel(ch -> consumer.accept(bus, ch));
    }

    @Override
    public <R> R call(IOBiFunction<StdProtoBus, ProtoIO, R> function) {
        final AtomicReference<R> ref = new AtomicReference<>();
        doWithChannel(ch -> ref.set(function.apply(bus, ch)));
        return ref.get();
    }

    private void doWithChannel(IOConsumer<ProtoIO> consumer) {
        synchronized (bus.scheduler) {
            try {
                bus.init();
                consumer.accept(bus.io);
                bus.health.successfulTransactionCount.incrementAndGet();
                bus.health.lastSuccessfulTransactionTimestamp.set(System.currentTimeMillis());
            } catch (Exception x) {
                bus.health.failedTransactionCount.incrementAndGet();
                bus.health.lastFailedTransactionTimestamp.set(System.currentTimeMillis());
                bus.log(WARNING, "Error", x);
            }
        }
    }
}
