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

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class TransceiverMethods {

    public static IoContext data(Transceiver transceiver) {
        return new IoContext(transceiver);
    }

    public static final class IoContext {

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

        @SafeVarargs
        public final Object read(Function<ByteBuffer, Object>... rules) throws TimeoutException, IOException {
            Collections.addAll(this.rules, rules);
            final FastArrayOutputStream os = new FastArrayOutputStream(1024);
            if (timeoutDriven) {
                final byte[] buf = new byte[1024];
                for (long t = System.currentTimeMillis(); System.currentTimeMillis() - t < timeout; ) {
                    try {
                        final int n = transceiver.read(buf, 0, buf.length);
                        if (n < 0) {
                            throw new EOFException();
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
                            throw new EOFException();
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
            throw new TimeoutException();
        }

        private Object read(FastArrayOutputStream fos) {
            final ByteBuffer buffer = ByteBuffer.wrap(fos.toByteArray());
            for (final Function<ByteBuffer, Object> rule : rules) {
                final Object v = rule.apply(buffer);
                if (v != null) {
                    return v;
                } else {
                    buffer.clear();
                }
            }
            return null;
        }
    }
}
