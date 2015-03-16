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

package org.marid.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

/**
* @author Dmitry Ovchinnikov
*/
public final class IoContext {

    public final Transceiver transceiver;
    public final List<Function<ByteBuffer, Object>> rules = new LinkedList<>();
    public boolean timeoutDriven = true;
    public long timeout = 1_000;

    public IoContext(Transceiver transceiver) {
        this.transceiver = transceiver;
    }

    public IoContext timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public IoContext timeoutDriven(boolean timeoutDriven) {
        this.timeoutDriven = timeoutDriven;
        return this;
    }

    public IoContext rule(Function<ByteBuffer, Object> rule) {
        rules.add(rule);
        return this;
    }

    public Object read(Function<ByteBuffer, Object> rule) throws IOException {
        rules.add(rule);
        return read();
    }

    public Object read() throws IOException {
        final FastArrayOutputStream os = new FastArrayOutputStream(1024);
        if (timeoutDriven) {
            final byte[] buf = new byte[1024];
            for (long t = System.currentTimeMillis(); System.currentTimeMillis() - t < timeout; ) {
                try {
                    final int n = transceiver.read(buf, 0, buf.length);
                    if (n < 0) {
                        throw new ClosedChannelException();
                    }
                    if (n > 0) {
                        os.write(buf, 0, n);
                        final Object v = read(os);
                        if (v != null) {
                            return v;
                        }
                    }
                } catch (InterruptedIOException x) {
                    LockSupport.parkNanos(1L);
                }
            }
        } else {
            for (long t = System.currentTimeMillis(); System.currentTimeMillis() - t < timeout; ) {
                final int size = transceiver.available();
                if (size > 0) {
                    final byte[] data = new byte[size];
                    final int n = transceiver.read(data, 0, size);
                    if (n < 0) {
                        throw new ClosedChannelException();
                    }
                    if (n > 0) {
                        os.write(data, 0, n);
                        final Object v = read(os);
                        if (v != null) {
                            return v;
                        }
                        LockSupport.parkNanos(1L);
                    }
                }
            }
        }
        throw new InterruptedIOException();
    }

    public void writeInt(int data) throws IOException {
        transceiver.write(ByteBuffer.allocate(4).putInt(0, data).array(), 0, 4);
    }

    public void writeLong(long data) throws IOException {
        transceiver.write(ByteBuffer.allocate(8).putLong(0, data).array(), 0, 8);
    }

    public void writeShort(short data) throws IOException {
        transceiver.write(ByteBuffer.allocate(2).putShort(0, data).array(), 0, 2);
    }

    public void writeChar(char data) throws IOException {
        transceiver.write(ByteBuffer.allocate(2).putChar(0, data).array(), 0, 2);
    }

    public void writeAscii(String data) throws IOException {
        final byte[] bytes = data.getBytes(StandardCharsets.US_ASCII);
        transceiver.write(bytes, 0, bytes.length);
    }

    public void writeUtf8(String data) throws IOException {
        final byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        transceiver.write(bytes, 0, bytes.length);
    }

    private Object read(FastArrayOutputStream fos) {
        final ByteBuffer buffer = ByteBuffer.wrap(fos.toByteArray());
        for (final Function<ByteBuffer, Object> rule : rules) {
            final Object v = rule.apply(buffer);
            if (v != null) {
                fos.reset();
                return v;
            } else {
                buffer.clear();
            }
        }
        return null;
    }
}
