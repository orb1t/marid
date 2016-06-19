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

package org.marid.io;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProcessManager implements Closeable {

    private final String name;
    private final Process process;
    private final long waitTimeout;
    private final TimeUnit timeUnit;
    private final InputStream out;
    private final InputStream err;
    private final Thread outConsumer;
    private final Thread errConsumer;

    public ProcessManager(String name,
                          Process process,
                          Consumer<String> outLineConsumer,
                          Consumer<String> errLineConsumer,
                          int bufferSize,
                          long waitTimeout,
                          TimeUnit timeUnit) {
        this.name = name;
        this.process = process;
        this.waitTimeout = waitTimeout;
        this.timeUnit = timeUnit;
        this.out = process.getInputStream();
        this.err = process.getErrorStream();
        this.outConsumer = new ConsumerThread(name + "(out)", out, bufferSize, outLineConsumer);
        this.errConsumer = new ConsumerThread(name + "(err)", err, bufferSize, errLineConsumer);
        outConsumer.start();
        errConsumer.start();
    }

    public ProcessManager(String name, Process process, Consumer<String> outLineConsumer, Consumer<String> errLineConsumer) {
        this(name, process, outLineConsumer, errLineConsumer, 65536, 1L, TimeUnit.MINUTES);
    }

    private void awaitThread(Thread thread) throws IOException {
        try {
            thread.join();
        } catch (InterruptedException x) {
            throw new InterruptedIOException(thread + " joining interrupted");
        }
    }

    private void awaitThreads(IOException iox) throws IOException {
        try (final InputStream e = err; final InputStream i = out) {
            try {
                awaitThread(errConsumer);
                awaitThread(outConsumer);
            } catch (IOException x) {
                if (iox == null) {
                    iox = x;
                } else {
                    iox.addSuppressed(x);
                }
            }
            if (iox != null) {
                throw iox;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (process.isAlive()) {
            process.destroy();
            try {
                boolean result = process.waitFor(waitTimeout, timeUnit);
                if (!result) {
                    process.destroyForcibly();
                    result = process.waitFor(waitTimeout, timeUnit);
                    if (!result) {
                        awaitThreads(new StreamCorruptedException("Zombie process: " + name));
                    }
                }
            } catch (InterruptedException x) {
                awaitThreads(new InterruptedIOException(process.toString()));
            }
        }
        awaitThreads(null);
    }

    static class ConsumerThread extends Thread {

        private final InputStream inputStream;
        private final Consumer<String> lineConsumer;
        private final int bufferSize;

        ConsumerThread(String name, InputStream inputStream, int bufferSize, Consumer<String> lineConsumer) {
            super(null, null, name, 96L * 1024L);
            this.inputStream = inputStream;
            this.lineConsumer = lineConsumer;
            this.bufferSize = bufferSize;
        }

        @Override
        public void run() {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), bufferSize)) {
                while (true) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lineConsumer.accept(line);
                }
            } catch (IOException x) {
                throw new UncheckedIOException("Unexpected exception: " + getName(), x);
            }
        }
    }
}
