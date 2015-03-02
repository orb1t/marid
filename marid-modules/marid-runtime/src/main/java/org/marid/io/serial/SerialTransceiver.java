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

package org.marid.io.serial;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.marid.io.Transceiver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SerialTransceiver implements Transceiver {

    private final SerialTransceiverParameters parameters;

    private SerialPort serialPort;

    public SerialTransceiver(SerialTransceiverParameters parameters) {
        this.parameters = parameters;
    }

    public SerialTransceiver(Map<String, Object> parameters) {
        this(new SerialTransceiverParameters(parameters));
    }

    @Override
    public synchronized boolean isValid() {
        return serialPort != null;
    }

    @Override
    public synchronized void open() throws IOException {
        if (serialPort == null) {
            try {
                serialPort = new SerialPort(parameters.getName());
            } catch (Exception x) {
                throw new IOException(x);
            } finally {
                serialPort = null;
            }
        }
    }

    @Override
    public synchronized void write(byte[] data, int offset, int len) throws IOException {
        apply(() -> serialPort.writeBytes(Arrays.copyOfRange(data, offset, offset + len)));
    }

    @Override
    public synchronized int read(byte[] data, int offset, int len) throws IOException {
        final long timeout = parameters.getTimeout();
        return apply(() -> {
            for (long t = currentTimeMillis(); currentTimeMillis() - t < timeout && serialPort.isOpened(); ) {
                final int l = serialPort.getInputBufferBytesCount();
                if (l > 0) {
                    final int n = Math.min(len, l);
                    final byte[] buf = serialPort.readBytes(n);
                    System.arraycopy(buf, 0, data, offset, n);
                    return n;
                } else {
                    Thread.sleep(1L);
                }
            }
            return -1;
        });
    }

    @Override
    public synchronized int available() throws IOException {
        return apply(serialPort::getInputBufferBytesCount);
    }

    @Override
    public synchronized void close() throws IOException {
        if (serialPort != null) {
            try {
                serialPort.closePort();
            } catch (Exception x) {
                throw new IOException(x);
            }
        }
    }

    synchronized <T> T apply(SerialPortAction<T> action) throws IOException {
        try {
            return action.apply();
        } catch (InterruptedException x) {
            final InterruptedIOException y = new InterruptedIOException(x.getMessage());
            y.initCause(x);
            throw y;
        } catch (SerialPortException x) {
            throw new IOException(x);
        }
    }

    @FunctionalInterface
    interface SerialPortAction<T> {

        T apply() throws SerialPortException, InterruptedException;
    }
}
