/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.daemon;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.marid.daemon.Log.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Daemon {

    private static final Logger LOG = Logger.getLogger(Daemon.class.getName());
    static final int PORT = ParseUtils.getInt("MARID.DAEMON.PORT", 11200);
    static final int BACKLOG = ParseUtils.getInt("MARID.DAEMON.BACKLOG", 5);
    static final String ADDRESS = ParseUtils.getString("MARID.DAEMON.ADDRESS", "localhost");
    static final File TARGET = ParseUtils.getDir("MARID.DAEMON.TARGET", null, "marid");
    static final File VMARGS = ParseUtils.getFile("MARID.DAEMON.VM.ARGS", TARGET, "ext", "vm.args");
    static final File ARGS = ParseUtils.getFile("MARID.DAEMON.ARGS", TARGET, "ext", "marid.args");
    static final File OUT = ParseUtils.getFile("MARID.DAEMON.OUT", TARGET, "marid.log");
    static final File ERR = ParseUtils.getFile("MARID.DAEMON.ERR", TARGET, "marid.err");
    static final File BACKUPS = ParseUtils.getDir("MARID.DAEMON.BACKUPS", null, "maridbk");
    static final File CUR_FILE = ParseUtils.getFile("MARID.DAEMON.CUR.FILE", BACKUPS, "cur.zip");
    static final int THREADS = ParseUtils.getInt("MARID.DAEMON.THREADS", 64);
    static final long KEEP_ALIVE = ParseUtils.getLong("MARID.DAEMON.KEEP.ALIVE", 60L);

    static Process maridProcess;

    public static void main(String... args) throws Exception {
        try (final InputStream logProps = Daemon.class.getResourceAsStream("/log.properties")) {
            if (logProps != null) {
                LogManager.getLogManager().readConfiguration(logProps);
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                warning(LOG, "Unhandled exception in {0}", e, t);
            }
        });
        final ServerSocketFactory serverSocketFactory = SSLServerSocketFactory.getDefault();
        LOG.log(Level.INFO, "Server socket factory: {0}", serverSocketFactory);
        final ServerSocket serverSocket = serverSocketFactory.createServerSocket(
                PORT, BACKLOG, InetAddress.getByName(ADDRESS));
        LOG.log(Level.INFO, "Server socket: {0}", serverSocket);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, THREADS, KEEP_ALIVE,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new CallerRunsPolicy());
        LOG.log(Level.INFO, "Executor: {0}", executor);
        final ExecutorCompletionService<Session> completionService =
                new ExecutorCompletionService<>(executor);
        final Thread scavengerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final Future<Session> future;
                    try {
                        future = completionService.take();
                    } catch (InterruptedException x) {
                        break;
                    }
                    final Session session;
                    try {
                        session = future.get();
                    } catch (InterruptedException x) {
                        break;
                    } catch (ExecutionException x) {
                        severe(LOG, "Unknown exception", x.getCause());
                        continue;
                    }
                    info(session.getLog(), "Terminated");
                }
            }
        });
        scavengerThread.setDaemon(true);
        scavengerThread.start();
        while (!executor.isShutdown()) {
            final Socket socket = serverSocket.accept();
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).addHandshakeCompletedListener(new HandshakeCompletedListener() {
                    @Override
                    public void handshakeCompleted(HandshakeCompletedEvent event) {
                        final Session session = new Session(event.getSocket());
                        info(session.getLog(), "New client");
                        event.getSocket().removeHandshakeCompletedListener(this);
                        completionService.submit(session);
                    }
                });
            } else {
                warning(LOG, "Non-SSL socket {0}", socket);
            }
        }
    }

    static synchronized void run() throws Exception {
        if (maridProcess != null) {
            return;
        }
        List<String> args = new LinkedList<>();
        args.add(getJavaBinary());
        if (VMARGS.exists()) {
            args.addAll(Files.readAllLines(VMARGS.toPath(), StandardCharsets.UTF_8));
        }
        args.add("-jar");
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(TARGET.toPath(), "marid-*.jar")) {
            args.add(ds.iterator().next().getFileName().toString());
        }
        if (ARGS.exists()) {
            args.addAll(Files.readAllLines(ARGS.toPath(), StandardCharsets.UTF_8));
        }
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectOutput(OUT);
        processBuilder.redirectError(ERR);
        processBuilder.directory(TARGET);
        maridProcess = processBuilder.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int resultCode = maridProcess.waitFor();
                    info(LOG, "Marid was terminated with exit code {0}", resultCode);
                } catch (Exception x) {
                    warning(LOG, "Waiting process error", x);
                }
            }
        }).start();
    }

    static synchronized void stop() throws Exception {
        if (maridProcess != null) {
            try {
                maridProcess.destroy();
            } finally {
                maridProcess = null;
            }
        }
    }

    private static String getJavaBinary() {
        return ParseUtils.getString("MARID.DAEMON.JAVA.BIN",
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    }
}
