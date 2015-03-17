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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.function.Supplier;

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
                    if (n < 0 || Thread.interrupted()) {
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
                    if (n < 0 || Thread.interrupted()) {
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

    private void write0(Collection<?> list, DataOutputStream dos) throws IOException {
        for (final Object element : list) {
            final Object e;
            if (element instanceof Callable) {
                try {
                    e = ((Callable) element).call();
                } catch (Exception x) {
                    throw new IllegalArgumentException();
                }
            } else if (element instanceof Supplier) {
                e = ((Supplier) element).get();
            } else {
                e = element;
            }
            if (e instanceof Integer) {
                dos.writeInt((int) e);
            } else if (e instanceof Long) {
                dos.writeLong((long) e);
            } else if (e instanceof Byte) {
                dos.writeByte((byte) e);
            } else if (e instanceof Short) {
                dos.writeShort((short) e);
            } else if (e instanceof Character) {
                dos.writeChar((char) e);
            } else if (e instanceof Float) {
                dos.writeFloat((float) e);
            } else if (e instanceof Double) {
                dos.writeDouble((double) e);
            } else if (e instanceof ByteBuffer) {
                final byte[] buf = new byte[((ByteBuffer) e).remaining()];
                ((ByteBuffer) e).mark();
                ((ByteBuffer) e).get(buf);
                ((ByteBuffer) e).reset();
                dos.write(buf);
            } else if (e instanceof byte[]) {
                dos.write((byte[]) e);
            } else if (e instanceof String) {
                dos.write(((String) e).getBytes(StandardCharsets.US_ASCII));
            } else if (e instanceof char[]) {
                dos.writeChars(new String((char[]) e));
            } else if (e instanceof int[]) {
                for (final int v : (int[]) e) {
                    dos.writeInt(v);
                }
            } else if (e instanceof double[]) {
                for (final double v : (double[]) e) {
                    dos.writeDouble(v);
                }
            } else if (e instanceof float[]) {
                for (final float v : (float[]) e) {
                    dos.writeFloat(v);
                }
            } else if (e instanceof long[]) {
                for (final long v : (long[]) e) {
                    dos.writeLong(v);
                }
            } else if (e instanceof short[]) {
                for (final short v : (short[]) e) {
                    dos.writeShort(v);
                }
            } else if (e instanceof Boolean) {
                dos.writeBoolean((boolean) e);
            } else if (e instanceof boolean[]) {
                for (final boolean v : (boolean[]) e) {
                    dos.writeBoolean(v);
                }
            } else if (e instanceof BitSet) {
                dos.write(((BitSet) e).toByteArray());
            } else if (e instanceof BigInteger) {
                dos.write(((BigInteger) e).toByteArray());
            } else if (e instanceof UUID) {
                dos.writeLong(((UUID) e).getMostSignificantBits());
                dos.writeLong(((UUID) e).getLeastSignificantBits());
            } else if (e instanceof Collection) {
                write0((Collection) e, dos);
            }
        }
    }

    public void write(Collection<?> list) throws IOException {
        final FastArrayOutputStream os = new FastArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(os);
        write0(list, dos);
        if (os.size() > 0) {
            transceiver.write(os.getSharedBuffer(), 0, os.size());
        }
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
