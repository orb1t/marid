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

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.marid.wrapper.Log.info;
import static org.marid.wrapper.Log.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper implements UncaughtExceptionHandler {

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());
    private static final String DEFAULT_SSF = MaridServerSocketFactory.class.getCanonicalName();
    static final int PORT = ParseUtils.getInt("MARID_DAEMON_PORT", 11200);
    static final int BACKLOG = ParseUtils.getInt("MARID_DAEMON_BACKLOG", 5);
    static final String ADDRESS = ParseUtils.getString("MARID_DAEMON_ADDRESS", "localhost");
    static final File TARGET = ParseUtils.getDir("MARID_DAEMON_TARGET", null, "marid");
    static final File VMARGS = ParseUtils.getFile("MARID_DAEMON_VM_ARGS", TARGET, "ext", "vm.args");
    static final File ARGS = ParseUtils.getFile("MARID_DAEMON_ARGS", TARGET, "ext", "marid.args");
    static final File OUT = ParseUtils.getFile("MARID_DAEMON_OUT", TARGET, "marid.log");
    static final File ERR = ParseUtils.getFile("MARID_DAEMON_ERR", TARGET, "marid.err");
    static final File BACKUPS = ParseUtils.getDir("MARID_DAEMON_BACKUPS", null, "maridbk");
    static final File CUR_FILE = ParseUtils.getFile("MARID_DAEMON_CUR_FILE", BACKUPS, "cur.zip");
    static final String SSF = ParseUtils.getString("MARID_DAEMON_SSF", DEFAULT_SSF);

    static Process maridProcess;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String... args) throws Exception {
        try (final InputStream logProps = Wrapper.class.getResourceAsStream("/log.properties")) {
            if (logProps != null) {
                LogManager.getLogManager().readConfiguration(logProps);
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(new Wrapper());
        final ClassLoader classLoader = getClassLoader();
        final ServerSocketFactory ssf = (ServerSocketFactory) classLoader.loadClass(SSF).newInstance();
        LOG.log(Level.INFO, "Server socket factory: {0}", ssf);
        final ServerSocket serverSocket = ssf.createServerSocket(PORT, BACKLOG, InetAddress.getByName(ADDRESS));
        LOG.log(Level.INFO, "Server socket: {0}", serverSocket);
        while (true) {
            try (final Socket socket = serverSocket.accept()) {
                final SSLSocket sslSocket = (SSLSocket) socket;
                final SSLSession session = sslSocket.getSession();
                info(LOG, "{0} New client", sslSocket);
                try {
                    new Session(sslSocket).call();
                } catch (Exception x) {
                    warning(LOG, "{0} Session error", x);
                }
            }
        }
    }

    static ClassLoader getClassLoader() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader != null ? classLoader : Wrapper.class.getClassLoader();
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

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        warning(LOG, "Unhandled exception in {0}", e, t);
    }
}
