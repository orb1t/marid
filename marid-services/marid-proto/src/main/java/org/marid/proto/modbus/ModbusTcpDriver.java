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

package org.marid.proto.modbus;

import org.marid.misc.Casts;
import org.marid.proto.ProtoBus;
import org.marid.proto.ProtoDriver;
import org.marid.proto.StdProto;

import javax.annotation.PostConstruct;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.System.currentTimeMillis;
import static java.nio.ByteBuffer.allocate;
import static java.util.concurrent.locks.LockSupport.parkNanos;

/**
 * @author Dmitry Ovchinnikov
 */
public class ModbusTcpDriver extends StdProto implements ProtoDriver {

    private final ProtoBus bus;
    private final char transactionIdentifier;
    private final char slaveAndFunc;
    private final char address;
    private final char count;
    private final long timeout;
    private final long errorTimeout;
    private final long delay;
    private final long period;
    private final TimeUnit timeUnit;
    private final ByteOrder byteOrder;
    private final Consumer<char[]> consumer;

    private ScheduledFuture<?> task;

    public ModbusTcpDriver(ProtoBus bus, String id, String name, ModbusTcpDriverProps props, Consumer<char[]> consumer) {
        super(id, name);
        this.bus = bus;
        this.consumer = consumer;
        this.bus.getChildren().put(id, Casts.cast(this));
        this.transactionIdentifier = (char) bus.getChildren().size();
        this.slaveAndFunc = allocate(2).put(0, (byte) props.getUnitId()).put(1, (byte) props.getFunc()).getChar(0);
        this.address = (char) props.getAddress();
        this.count = (char) props.getCount();
        this.timeout = props.getTimeout();
        this.errorTimeout = props.getErrorTimeout();
        this.delay = props.getDelay();
        this.period = props.getPeriod();
        this.timeUnit = props.getTimeUnit();
        this.byteOrder = props.getByteOrder();
    }

    @PostConstruct
    @Override
    public synchronized void start() {
        if (task != null) {
            return;
        }
        task = bus.getTaskRunner().schedule((b, ch) -> {
            {
                final ByteBuffer out = ByteBuffer.allocate(12)
                        .putChar(transactionIdentifier)
                        .putChar((char) 0)
                        .putChar((char) 6)
                        .putChar(slaveAndFunc)
                        .putChar(address)
                        .putChar(count);
                while (out.remaining() > 0) {
                    ch.write(out);
                }
            }
            {
                final ByteBuffer in = ByteBuffer.allocate(8 + 2 * count);
                final long start = System.currentTimeMillis();
                while (currentTimeMillis() - start < timeout) {
                    ch.read(in);
                    if (in.remaining() == 0) {
                        break;
                    }
                }
                if (in.remaining() > 0) {
                    parkNanos(TimeUnit.MILLISECONDS.toNanos(errorTimeout));
                    final ByteBuffer buffer = ByteBuffer.allocate(512);
                    while (ch.read(buffer) > 0) {
                        buffer.clear();
                    }
                } else {
                    in.flip();
                    final char ti = in.getChar();
                    if (ti != transactionIdentifier) {
                        throw new StreamCorruptedException("Invalid transaction identifier: " + (int) ti);
                    }
                    final char pi = in.getChar();
                    if (pi != 0) {
                        throw new StreamCorruptedException("Invalid protocol: " + (int) pi);
                    }
                    final char size = in.getChar();
                    if (size != 2 * count) {
                        throw new StreamCorruptedException("Incorrect size: " + (int) size);
                    }
                    final char saf = in.getChar();
                    if (saf != slaveAndFunc) {
                        throw new StreamCorruptedException("Incorrect slave and func: " + Integer.toHexString(saf));
                    }
                    in.order(byteOrder);
                    final char[] data = new char[count];
                    for (int i = 0; i < count; i++) {
                        data[i] = in.getChar();
                    }
                    consumer.accept(data);
                }
            }
        }, delay, period, timeUnit, false);
    }

    @Override
    public synchronized void close() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return task != null;
    }

    @Override
    public ProtoBus getParent() {
        return bus;
    }
}
