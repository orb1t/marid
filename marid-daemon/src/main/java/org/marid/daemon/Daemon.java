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
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.logging.Level;
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
    static final File OUT = ParseUtils.getFile("MARID.DAEMON.OUT", TARGET, "marid.log");
    static final File BACKUPS = ParseUtils.getDir("MARID.DAEMON.BACKUPS", null, "maridbk");
    static final File TEMP_FILE = ParseUtils.getFile("MARID.DAEMON.TEMP.FILE", BACKUPS, "temp.zip");
    static final File CUR_FILE = ParseUtils.getFile("MARID.DAEMON.CUR.FILE", BACKUPS, "cur.zip");
    static final int THREADS = ParseUtils.getInt("MARID.DAEMON.THREADS", 64);
    static final long KEEP_ALIVE = ParseUtils.getLong("MARID.DAEMON.KEEP.ALIVE", 60L);

    public static void main(String... args) throws Exception {
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
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, THREADS, KEEP_ALIVE,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new CallerRunsPolicy());
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
}
