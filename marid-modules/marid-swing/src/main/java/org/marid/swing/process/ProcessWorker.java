/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.process;

import javax.swing.*;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.System.currentTimeMillis;
import static org.marid.swing.process.ProcessWorker.ProcessLine;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProcessWorker extends SwingWorker<Integer, ProcessLine> {

    private final ProcessBuilder processBuilder;
    private final long timeout;
    private final ThreadGroup threadGroup;
    protected final ConcurrentLinkedQueue<Exception> errorQueue = new ConcurrentLinkedQueue<>();
    private Process process;

    public ProcessWorker(ProcessBuilder processBuilder, long timeout) {
        this.processBuilder = processBuilder;
        this.timeout = timeout;
        this.threadGroup = new ThreadGroup(processBuilder.toString());
    }

    public void terminate() {
        process.destroy();
    }

    @Override
    protected Integer doInBackground() throws Exception {
        process = processBuilder.start();
        final Thread out = new Thread(threadGroup, consumeTask(process.getInputStream(), false), "out", 64L * 1024L);
        final Thread err = new Thread(threadGroup, consumeTask(process.getErrorStream(), true), "err", 64L * 1024L);
        out.start();
        err.start();
        try {
            while (!isDone() && errorQueue.isEmpty() && out.isAlive() && err.isAlive()) {
                Thread.sleep(100L);
            }
        } catch (Exception x) {
            errorQueue.add(x);
        }
        process.destroy();
        for (final long startTime = currentTimeMillis(); currentTimeMillis() - startTime <= timeout; ) {
            if (process.isAlive()) {
                Thread.sleep(100L);
            } else {
                break;
            }
        }
        final int exitValue = process.isAlive() ? process.destroyForcibly().waitFor() : process.exitValue();
        errorQueue.add(new ProcessExitCode(processBuilder.command(), exitValue));
        return exitValue;
    }

    private Runnable consumeTask(InputStream inputStream, boolean error) {
        return () -> {
            try (final Scanner scanner = new Scanner(inputStream)) {
                while (!isCancelled() && !isDone() && scanner.hasNextLine()) {
                    publish(new ProcessLine(error, scanner.nextLine()));
                    if (scanner.ioException() != null) {
                        errorQueue.offer(scanner.ioException());
                        break;
                    }
                }
            }
        };
    }

    protected static class ProcessLine {

        public final boolean error;
        public final String line;

        public ProcessLine(boolean error, String line) {
            this.error = error;
            this.line = line;
        }
    }

    protected static class ProcessExitCode extends RuntimeException {

        public ProcessExitCode(List<String> command, int code) {
            super(code + ": " + String.join(" ", command), null, false, false);
        }
    }
}
