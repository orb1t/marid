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
import org.hsqldb.jdbc.JDBCSessionConnection;
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
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.locks.LockSupport;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Dmitry Ovchinnikov.
 */
@MaridBean(icon = "O.DATABASE")
public final class HsqldbDatabase implements Closeable, LogSupport {

    private final Server server;
    private final File directory;
    private final URI sqlDirectoryUri;
    private final long shutdownTimeout;
    private final int connectionPoolSize;
    private final Map<String, Integer> databaseNameToIndex = new TreeMap<>();

    private PrintWriter outWriter;
    private PrintWriter errWriter;

    public HsqldbDatabase(HsqldbProperties properties) {
        directory = properties.getDirectory();
        sqlDirectoryUri = properties.getSqlDirectoryUri();
        shutdownTimeout = SECONDS.toMillis(properties.getShutdownTimeoutSeconds());
        connectionPoolSize = properties.getConnectionPoolSize();
        server = new Server();
        server.setNoSystemExit(true);
        server.setRestartOnShutdown(false);
        server.setDaemon(true);
        setDatabase("numerics");
    }

    private void setDatabase(String name) {
        final int index = databaseNameToIndex.size();
        server.setDatabaseName(index, name);
        server.setDatabasePath(index, new File(directory, name).getAbsolutePath());
        databaseNameToIndex.put(name, index);
    }

    @PostConstruct
    public void init() throws IOException {
        outWriter = new PrintWriter(new File(directory, "output.log"));
        errWriter = new PrintWriter(new File(directory, "errors.log"));
        server.start();
        for (final String db : databaseNameToIndex.keySet()) {
            try {
                initDatabase(db);
            } catch (IOException | SQLException x) {
                log(WARNING, "Unable to init DB {0}", x, db);
            }
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        close();
    }

    private void initDatabase(String name) throws SQLException, IOException {
        final int index = databaseNameToIndex.get(name);
        final Database database = DatabaseManager.getDatabase(index);
        try (final Connection c = new JDBCSessionConnection(database, "PUBLIC")) {
            c.setAutoCommit(true);
            final boolean tableExists;
            try (final ResultSet rs = c.getMetaData().getTables(null, null, name.toUpperCase(), new String[] {"TABLE"})) {
                tableExists = rs.next();
            }
            if (tableExists) {
                log(INFO, "Table {0} already exists in DB {1}", name.toUpperCase(), name);
                return;
            }
            try (final Statement s = c.createStatement()) {
                final File sqlFile = new File(sqlDirectoryUri.resolve(name + ".sql"));
                if (sqlFile.length() > 0L) {
                    try (final Scanner scanner = new Scanner(sqlFile)) {
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
        final int dbIndex = databaseNameToIndex.get(name);
        final Database database = DatabaseManager.getDatabase(dbIndex);
        return connectionPoolSize == 0
                ? new JDBCSessionDataSource(database, name.toUpperCase())
                : new JDBCSessionPool(connectionPoolSize, database, name.toUpperCase());
    }

    @MaridBean(icon = "O.DATABASE")
    public NumericWriter numericWriter() {
        return new HsqldbDaqNumericWriter(getDataSource("numerics"), "NUMERICS");
    }
}
