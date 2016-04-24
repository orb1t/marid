/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.db.hsqldb;

import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.jdbc.JDBCSessionDataSource;
import org.hsqldb.jdbc.JDBCSessionPool;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerConstants;
import org.marid.beans.MaridBean;
import org.marid.db.dao.NumericWriter;
import org.marid.logging.LogSupport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Dmitry Ovchinnikov.
 */
@MaridBean(icon = "http://icons.iconarchive.com/icons/icojam/blue-bits/24/database-icon.png")
public final class HsqldbDatabase implements Closeable, LogSupport {

    private final Server server;
    private final File directory;
    private final long shutdownTimeout;
    private final int connectionPoolSize;
    private final Map<String, URL> databaseNameToIndex = new LinkedHashMap<>();

    private PrintWriter outWriter;
    private PrintWriter errWriter;

    public HsqldbDatabase(HsqldbProperties properties) {
        log(INFO, "{0}", properties);
        directory = properties.getDirectory();
        shutdownTimeout = SECONDS.toMillis(properties.getShutdownTimeoutSeconds());
        connectionPoolSize = properties.getConnectionPoolSize();
        server = new Server();
        server.setNoSystemExit(true);
        server.setRestartOnShutdown(false);
        setDatabase("NUMERICS", properties.getNumericsSql());
    }

    private void setDatabase(String name, URL url) {
        final int index = databaseNameToIndex.size();
        server.setDatabaseName(index, name);
        server.setDatabasePath(index, new File(directory, name).getAbsolutePath());
        databaseNameToIndex.put(name, url);
    }

    @PostConstruct
    public void init() throws IOException {
        outWriter = new PrintWriter(new File(directory, "output.log"));
        errWriter = new PrintWriter(new File(directory, "errors.log"));
        server.start();
        for (final Map.Entry<String, URL> e : databaseNameToIndex.entrySet()) {
            try {
                initDatabase(e.getKey(), e.getValue());
            } catch (IOException | SQLException x) {
                log(WARNING, "Unable to init DB {0}", x, e.getKey());
            }
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        close();
    }

    private void initDatabase(String name, URL url) throws SQLException, IOException {
        try (final Connection c = getDataSource(name).getConnection()) {
            c.setAutoCommit(true);
            final boolean tableExists;
            try (final ResultSet rs = c.getMetaData().getTables(null, null, name, new String[]{"TABLE"})) {
                tableExists = rs.next();
            }
            if (tableExists) {
                log(INFO, "Table {0} already exists", name);
                return;
            }
            try (final Statement s = c.createStatement()) {
                try (final Scanner scanner = new Scanner(url.openStream())) {
                    while (scanner.hasNextLine()) {
                        final String sql = scanner.nextLine().trim();
                        if (sql.startsWith("--") || sql.isEmpty()) {
                            continue;
                        }
                        try {
                            log(INFO, "Executing {0}", sql);
                            s.execute(sql);
                        } catch (SQLException x) {
                            log(WARNING, "Unable to execute '{0}'", x, sql);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try (final PrintWriter out = outWriter; final PrintWriter err = errWriter) {
            server.shutdown();
            for (final long startTime = currentTimeMillis(); currentTimeMillis() - startTime < shutdownTimeout; ) {
                if (server.getState() == ServerConstants.SERVER_STATE_SHUTDOWN) {
                    return;
                }
                LockSupport.parkNanos(MILLISECONDS.toNanos(10L));
            }
            throw new InterruptedIOException("Server shutdown timeout exceeded");
        }
    }

    private DataSource getDataSource(String name) {
        final int dbIndex = databaseNameToIndex.keySet().stream().collect(Collectors.toList()).indexOf(name);
        final Database database = DatabaseManager.getDatabase(dbIndex);
        return connectionPoolSize == 0
                ? new JDBCSessionDataSource(database, "PUBLIC")
                : new JDBCSessionPool(connectionPoolSize, database, "PUBLIC");
    }

    @MaridBean(icon = "http://icons.iconarchive.com/icons/double-j-design/ravenna-3d/24/Database-Table-icon.png")
    public NumericWriter numericWriter() {
        return new HsqldbDaqNumericWriter(getDataSource("NUMERICS"), "NUMERICS");
    }
}
