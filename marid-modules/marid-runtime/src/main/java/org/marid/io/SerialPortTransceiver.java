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

import jssc.SerialPort;
import jssc.SerialPortException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

/**
 * @author Dmitry Ovchinnikov
 */
public class SerialPortTransceiver implements Transceiver {

    private final SerialPort serialPort;
    private int timeout;

    public SerialPortTransceiver(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                final byte[] data = new byte[1];
                final int result = read(data);
                if (result < 0) {
                    return result;
                } else {
                    return Byte.toUnsignedInt(data[0]);
                }
            }

            @Override
            public int read(@Nonnull byte[] b, int off, int len) throws IOException {
                return apply(() -> {
                    for (long t = currentTimeMillis(); currentTimeMillis() - t < timeout && serialPort.isOpened(); ) {
                        final int l = serialPort.getInputBufferBytesCount();
                        if (l > 0) {
                            final int n = Math.min(len, l);
                            final byte[] data = serialPort.readBytes(n);
                            System.arraycopy(data, 0, b, off, n);
                            return n;
                        } else {
                            Thread.sleep(1L);
                        }
                    }
                    return -1;
                });
            }

            @Override
            public int available() throws IOException {
                return apply(serialPort::getInputBufferBytesCount);
            }

            @Override
            public void close() throws IOException {
                SerialPortTransceiver.this.close();
            }
        };
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[(byte) b]);
            }

            @Override
            public void write(@Nonnull byte[] b, int off, int len) throws IOException {
                apply(() -> serialPort.writeBytes(Arrays.copyOfRange(b, off, off + len)));
            }

            @Override
            public void close() throws IOException {
                SerialPortTransceiver.this.close();
            }
        };
    }

    @Override
    public synchronized boolean isValid() {
        return serialPort.isOpened();
    }

    @Override
    public synchronized void setTimeout(int timeout) throws IOException {
        this.timeout = timeout;
    }

    @Override
    public synchronized int getTimeout() throws IOException {
        return timeout;
    }

    @Override
    public void close() throws IOException {
        apply(serialPort::closePort);
    }

    private synchronized <T> T apply(SerialPortAction<T> action) throws IOException {
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
    private interface SerialPortAction<T> {

        T apply() throws SerialPortException, InterruptedException;
    }
}
