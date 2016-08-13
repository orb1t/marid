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

import java.nio.channels.ByteChannel;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoBusTaskRunner<T extends ProtoBus> {

    Future<?> runAsync(IOBiConsumer<T, ByteChannel> consumer);

    <R> Future<R> callAsync(IOBiFunction<T, ByteChannel, R> function);

    ScheduledFuture<?> schedule(IOBiConsumer<T, ByteChannel> task, long delay, long period, TimeUnit unit, boolean fair);

    void run(IOBiConsumer<T, ByteChannel> consumer);

    <R> R call(IOBiFunction<T, ByteChannel, R> function);
}
