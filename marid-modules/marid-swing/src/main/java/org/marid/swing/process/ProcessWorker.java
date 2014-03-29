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

import org.marid.logging.LogSupport;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static java.lang.System.currentTimeMillis;
import static org.marid.swing.process.ProcessWorker.ProcessLine;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProcessWorker extends SwingWorker<Integer, ProcessLine> implements LogSupport {

    protected final ProcessBuilder processBuilder;
    protected final long timeout;
    protected final ThreadGroup threadGroup;
    private Process process;

    public ProcessWorker(ProcessBuilder processBuilder, long timeout) {
        this.processBuilder = processBuilder;
        this.timeout = timeout;
        this.threadGroup = new ThreadGroup(processBuilder.toString());
    }

    public void terminate() {
        process.destroy();
    }

    protected Process newProcess() throws IOException {
        return processBuilder.start();
    }

    @Override
    protected Integer doInBackground() throws Exception {
        process = newProcess();
        final Thread out = new Thread(threadGroup, consumeTask(process.getInputStream(), false), "out", 64L * 1024L);
        final Thread err = new Thread(threadGroup, consumeTask(process.getErrorStream(), true), "err", 64L * 1024L);
        out.start();
        err.start();
        try {
            while (!isDone() && out.isAlive() && err.isAlive()) {
                Thread.sleep(100L);
            }
        } catch (InterruptedException x) {
            warning("Interrupted {0}", x, processBuilder);
        }
        terminate();
        for (final long startTime = currentTimeMillis(); currentTimeMillis() - startTime <= timeout; ) {
            if (process.isAlive()) {
                Thread.sleep(100L);
            } else {
                break;
            }
        }
        return process.isAlive() ? process.destroyForcibly().waitFor() : process.exitValue();
    }

    private Runnable consumeTask(InputStream inputStream, boolean error) {
        return () -> {
            try (final Scanner scanner = new Scanner(inputStream)) {
                while (!isCancelled() && !isDone() && scanner.hasNextLine()) {
                    publish(new ProcessLine(error, scanner.nextLine()));
                    final IOException ioException = scanner.ioException();
                    if (ioException != null) {
                        warning("I/O exception {0}", ioException, processBuilder);
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
}
