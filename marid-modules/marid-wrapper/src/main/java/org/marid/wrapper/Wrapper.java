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

import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.server.Server;
import org.marid.io.PrintStreamWriter;
import org.marid.logging.Logging;
import org.marid.net.UdpShutdownThread;
import org.marid.util.ShutdownCodes;
import org.marid.util.Utils;
import org.marid.wrapper.hsqldb.HsqldbConfiguration;
import org.marid.wrapper.hsqldb.HsqldbServerProvider;
import org.marid.wrapper.hsqldb.HsqldbWrapperServerDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.*;
import static org.hsqldb.server.ServerConstants.SERVER_STATE_ONLINE;
import static org.hsqldb.server.ServerConstants.SERVER_STATE_SHUTDOWN;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Wrapper {

    static {
        System.setProperty("hsqldb.reconfig_logging", Boolean.toString(false));
    }

    private static final Logger LOG = Logger.getLogger(Wrapper.class.getName());
    private static final PrintWriter ERR_WRITER = new PrintStreamWriter(System.err, true);

    public static void main(String... args) throws Exception {
        Logging.init(Wrapper.class, "marid-logging.properties");
        final WrapperCli cli = new WrapperCli(Utils.loadProperties(Wrapper.class, "marid-wrapper.properties"), args);
        if (cli.isHelp()) {
            cli.showHelp();
            return;
        }
        switch (cli.getCommand()) {
            case "start":
                start(cli);
                break;
            case "stop":
                stop(cli.getInstanceName(), cli.getBindAddress());
                break;
            default:
                throw new IllegalArgumentException(cli.getCommand());
        }
    }

    private static void start(WrapperCli cli) throws Exception {
        final UdpShutdownThread shutdownThread = new UdpShutdownThread(
                cli.getInstanceName(), () -> startDbServer(cli), cli.getBindAddress());
        try {
            shutdownThread.start();
            shutdownThread.join();
        } catch (Exception x) {
            warning(LOG, "Starting {0} error", x, cli.getInstanceName());
        }
        System.exit(shutdownThread.getExitCode());
    }

    private static void stop(String name, InetSocketAddress bindAddress) throws Exception {
        UdpShutdownThread.sendShutdownSequence(bindAddress, name);
    }

    private static HsqldbConfiguration newConfiguration(WrapperCli cli) {
        try {
            final HsqldbConfiguration configuration = cli.configuration()
                    .setNoSystemExit(true)
                    .setRestartOnShutdown(false);
            if (configuration.getDatabaseMap().isEmpty()) {
                final Path path = Paths.get(System.getProperty("user.home"), "marid-wrapper/marid-wrapper");
                configuration.putDatabase("wrapper", path.toString());
            }
            return configuration;
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    private static void startDbServer(WrapperCli cli) {
        final Server server = HsqldbServerProvider.getServer(newConfiguration(cli), ERR_WRITER, ERR_WRITER);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            do {
                info(LOG, "Waiting for DB server stop");
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException x) {
                    warning(LOG, "{0} was interrupted", x, Thread.currentThread());
                }
            } while (server.getState() != SERVER_STATE_SHUTDOWN);
        }));
        server.start();
        final long startTime = System.currentTimeMillis();
        do {
            info(LOG, "Waiting for DB server start");
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException x) {
                warning(LOG, "{0} was interrupted", x, Thread.currentThread());
            }
            if (System.currentTimeMillis() - startTime > TimeUnit.HOURS.toMillis(1L)) {
                severe(LOG, "DB server start timeout error");
                System.exit(ShutdownCodes.DB_SERVER_FAILURE);
                return;
            }
        } while (server.getState() != SERVER_STATE_ONLINE);
        final Database database = DatabaseManager.getDatabase(0);
        final Path umapPath = Paths.get(database.getPath()).getParent().resolve("marid-wrapper-update.map");
        final HsqldbWrapperServerDao dao = new HsqldbWrapperServerDao(database.getSessionManager().getSysSession());
        if (!Files.exists(umapPath)) {
            try {
                applySettings(cli.getSettings(), dao);
                dao.initDefaultSchema();
            } catch (Exception x) {
                warning(LOG, "Unable to apply settings", x);
                System.exit(ShutdownCodes.DB_SERVER_FAILURE);
                return;
            }
        }
        try {
            applySqlIndex(dao, umapPath);
        } catch (Exception x) {
            severe(LOG, "System halt due to serious error", x);
            System.exit(ShutdownCodes.DB_SERVER_FAILURE);
        }
    }

    private static void applySettings(URL settings, HsqldbWrapperServerDao dao) throws Exception {
        try (final Scanner scanner = new Scanner(settings.openStream(), "UTF-8")) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                dao.update(line);
                info(LOG, "Executing {0}", line);
            }
        }
    }

    private static void applySqlIndex(HsqldbWrapperServerDao dao, Path umapPath) throws Exception {
        final Map<String, Set<String>> umap = new LinkedHashMap<>();
        if (Files.exists(umapPath)) {
            try (final Scanner scanner = new Scanner(umapPath, "UTF-8")) {
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine().trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    final String[] parts = line.split(":");
                    if (parts.length != 2) {
                        throw new IllegalStateException("Invalid line: " + line);
                    }
                    final Set<String> set = umap.computeIfAbsent(parts[0].trim(), k -> new LinkedHashSet<>());
                    set.add(parts[1].trim());
                }
            }
        }
        try (final Scanner scanner = new Scanner(Wrapper.class.getResourceAsStream("sql/sqlUpdate.txt"), "UTF-8")) {
            final Pattern updateItemPattern = Pattern.compile("---\\s+(.+)\\s+---");
            while (scanner.hasNextLine()) {
                final String l = scanner.nextLine().trim();
                if (l.isEmpty() || l.startsWith("#")) {
                    continue;
                }
                try (final Scanner s = new Scanner(Wrapper.class.getResourceAsStream("sql/" + l + ".sql"), "UTF-8")) {
                    final StringBuilder currentUpdate = new StringBuilder();
                    String updateItem = null;
                    final Consumer<String> consumer = item -> {
                        final Set<String> set = umap.computeIfAbsent(l, k -> new LinkedHashSet<>());
                        final String updateLine = l + " : " + item;
                        if (!set.contains(item)) {
                            info(LOG, "Executing {0}", updateLine);
                            dao.update(currentUpdate.toString());
                            try {
                                Files.write(umapPath, Collections.singleton(updateLine), APPEND, WRITE, CREATE);
                            } catch (IOException x) {
                                throw new IllegalStateException(x);
                            }
                        }
                    };
                    while (s.hasNextLine()) {
                        final String line = s.nextLine().trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        final Matcher matcher = updateItemPattern.matcher(line);
                        if (matcher.matches()) {
                            if (updateItem != null) {
                                consumer.accept(updateItem);
                            }
                            updateItem = matcher.group(1);
                            currentUpdate.setLength(0);
                        } else if (!line.startsWith("--")) {
                            Objects.requireNonNull(updateItem, "Invalid line without updateItem: " + line);
                            currentUpdate.append(line);
                            currentUpdate.append('\n');
                        }
                    }
                    if (currentUpdate.length() > 0 && updateItem != null) {
                        consumer.accept(updateItem);
                    }
                }
            }
        }
    }
}
