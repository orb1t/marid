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

package org.marid.wrapper;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.*;
import static org.marid.wrapper.SecureContext.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());
    static final int PORT = ParseUtils.getInt("MW_PORT", 11200);
    static final int BACKLOG = ParseUtils.getInt("MW_BACKLOG", 5);
    static final String ADDRESS = ParseUtils.getString("MW_ADDRESS", null);
    static final File TARGET = ParseUtils.getDir("MW_TARGET", null, "marid");
    static final File BACKUPS = ParseUtils.getDir("MW_BACKUPS", null, "maridbk");
    static final int THREADS = ParseUtils.getInt("MW_THREADS", 8);
    static final int MAX_THREADS = ParseUtils.getInt("MW_MAX_THREADS", THREADS);

    static final Lock processLock = new ReentrantLock();
    static Process maridProcess;

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Wrapper());
        info(LOG, "Server socket factory: {0}", SecureContext.SERVER_SOCKET_FACTORY);
        final SSLServerSocket serverSocket = (SSLServerSocket) (ADDRESS == null
                ? SERVER_SOCKET_FACTORY.createServerSocket(PORT, BACKLOG)
                : SERVER_SOCKET_FACTORY.createServerSocket(PORT, BACKLOG, InetAddress.getByName(ADDRESS)));
        info(LOG, "Server socket: {0}", serverSocket);
        serverSocket.setNeedClientAuth(true);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(THREADS, MAX_THREADS,
                1L, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new CallerRunsPolicy());
        try {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    try {
                        final SSLSocket sslSocket = (SSLSocket) socket;
                        final SSLSession sslSession = sslSocket.getSession();
                        info(LOG, "{0} New client", sslSocket);
                        executor.execute(new Session(sslSocket, sslSession));
                    } catch (Exception x) {
                        warning(LOG, "{0} Client error", x, socket);
                    }
                } catch (Exception x) {
                    severe(LOG, "Accept clients failed", x);
                    break;
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        warning(LOG, "Unhandled exception in {0}", e, t);
    }
}
