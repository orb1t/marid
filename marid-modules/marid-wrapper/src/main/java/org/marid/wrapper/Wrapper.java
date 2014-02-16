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

import org.marid.io.JaxbStreams;
import org.marid.io.ProcessUtils;
import org.marid.secure.SecureProfile;
import org.marid.wrapper.data.DeployConf;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.*;
import static org.marid.wrapper.WrapperConstants.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper extends TimerTask implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());

    static final int PORT = ParseUtils.getInt("MW_PORT", DEFAULT_PORT);
    static final int BACKLOG = ParseUtils.getInt("MW_BACKLOG", 5);
    static final String ADDRESS = ParseUtils.getString("MW_ADDRESS", null);
    static final Path TARGET = ParseUtils.getDir("MW_TARGET", null, "marid");
    static final Path BACKUPS = ParseUtils.getDir("MW_BACKUPS", null, "maridBackups");
    static final Path LOGS = ParseUtils.getDir("MW_LOGS", TARGET, "logs");
    static final int THREADS = ParseUtils.getInt("MW_THREADS", 8);
    static final int QUEUE_SIZE = ParseUtils.getInt("MW_QUEUE_SIZE", 16);
    static final int TIMEOUT = ParseUtils.getInt("MW_TIMEOUT", 3_600_000);
    static final Properties USERS = new Properties();

    static final Lock processLock = new ReentrantLock();
    private static Process maridProcess;

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Wrapper());
        try (final InputStream is = Wrapper.class.getResourceAsStream("users.properties")) {
            if (is != null) {
                USERS.load(is);
            }
        }
        final SSLServerSocketFactory ssf = SecureProfile.DEFAULT.getServerSocketFactory();
        info(LOG, "Server socket factory: {0}", ssf);
        final SSLServerSocket serverSocket = (SSLServerSocket) (ADDRESS == null
                ? ssf.createServerSocket(PORT, BACKLOG)
                : ssf.createServerSocket(PORT, BACKLOG, InetAddress.getByName(ADDRESS)));
        info(LOG, "Server socket: {0}", serverSocket);
        serverSocket.setNeedClientAuth(true);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, THREADS,
                1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(QUEUE_SIZE), new CallerRunsPolicy());
        try {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setSoTimeout(TIMEOUT);
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

    @Override
    public void run() {
        try {
            processLock.tryLock(1L, TimeUnit.MINUTES);
        } catch (InterruptedException x) {
            warning(LOG, "Interrupted timer", x);
            return;
        }
        try {
            if (maridProcess != null) {
                return;
            }
            final Path confFile = TARGET.resolve("configuration.xml");
            final DeployConf deployConf;
            if (Files.isReadable(confFile)) {
                try {
                    deployConf = JaxbStreams.readXml(DeployConf.class, confFile);
                } catch (Exception x) {
                    warning(LOG, "Unable to read deploy configuration from {0}", x, confFile);
                    try {
                        Thread.sleep(60_000L);
                    } catch (InterruptedException ix) {
                        warning(LOG, "Interrupted timer", ix);
                    }
                    return;
                }
            } else {
                deployConf = new DeployConf();
            }
            final List<String> cmdLine = new LinkedList<>();
            cmdLine.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
            cmdLine.addAll(deployConf.getVmArguments());
            cmdLine.add("-jar");
            for (final String file : TARGET.toFile().list()) {
                if (file.startsWith("marid-runtime") && file.endsWith(".jar")) {
                    cmdLine.add(file);
                    break;
                }
            }
            cmdLine.addAll(deployConf.getMaridArguments());
            maridProcess = new ProcessBuilder(cmdLine)
                    .redirectError(LOGS.resolve("error.log").toFile())
                    .redirectOutput(LOGS.resolve("output.log").toFile())
                    .directory(TARGET.toFile())
                    .start();
        } catch (Exception x) {
            warning(LOG, "Error while starting process", x);
        } finally {
            processLock.unlock();
        }
    }

    private static Properties secureProperties() {
        final Properties properties = new Properties();
        try (final InputStream inputStream = Wrapper.class.getResourceAsStream("/maridWrapperSecurity.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception x) {
            warning(LOG, "Unable to process security properties");
        }
        return properties;
    }

    static void destroyProcess() {
        if (maridProcess != null) {
            try {
                try (final OutputStream os = maridProcess.getOutputStream()) {
                    os.write('x');
                }
                final int r = ProcessUtils.joinProcess(maridProcess, 360_000L);
                if (r != 0) {
                    warning(LOG, "{0} was terminated with exit status {1}", maridProcess, r);
                }
            } catch (Exception x) {
                warning(LOG, "{0} Unable to send exit command", x, maridProcess);
                maridProcess.destroy();
            } finally {
                maridProcess = null;
            }
        }
    }
}
