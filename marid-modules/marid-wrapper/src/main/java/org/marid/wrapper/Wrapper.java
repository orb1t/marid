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
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());
    static final int PORT = ParseUtils.getInt("MW_PORT", 11200);
    static final int BACKLOG = ParseUtils.getInt("MW_BACKLOG", 5);
    static final String ADDRESS = ParseUtils.getString("MW_ADDRESS", null);
    static final Path TARGET = ParseUtils.getDir("MW_TARGET", null, "marid");
    static final Path BACKUPS = ParseUtils.getDir("MW_BACKUPS", null, "maridbk");
    static final int THREADS = ParseUtils.getInt("MW_THREADS", 8);
    static final int QUEUE_SIZE = ParseUtils.getInt("MW_QUEUE_SIZE", 16);

    static final Lock processLock = new ReentrantLock();
    static Process maridProcess;

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Wrapper());
        final SSLServerSocketFactory ssf = SecureContext.getServerSocketFactory();
        info(LOG, "Server socket factory: {0}", ssf);
        final SSLServerSocket serverSocket = (SSLServerSocket) (ADDRESS == null
                ? ssf.createServerSocket(PORT, BACKLOG)
                : ssf.createServerSocket(PORT, BACKLOG, InetAddress.getByName(ADDRESS)));
        info(LOG, "Server socket: {0}", serverSocket);
        serverSocket.setNeedClientAuth(true);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, THREADS,
                1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(QUEUE_SIZE), new CallerRunsPolicy());
        try {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setSoTimeout(3_600_000);
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
