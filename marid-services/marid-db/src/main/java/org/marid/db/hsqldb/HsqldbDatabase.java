/*-
 * #%L
 * marid-db
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.db.hsqldb;

import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.hsqldb.jdbc.JDBCSessionDataSource;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerConstants;
import org.marid.runtime.annotation.MaridBean;
import org.marid.runtime.annotation.MaridBeanProducer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
@MaridBean(name = "HSQLDB Database", icon = "D_DATABASE")
public final class HsqldbDatabase implements Closeable {

    private final Server server;
    private final File directory;
    private final long shutdownTimeout;
    private final Map<String, URL> databaseNameToIndex = new LinkedHashMap<>();

    private PrintWriter outWriter;
    private PrintWriter errWriter;

    @MaridBeanProducer(name = "HSQLDB Database", icon = "D_DATABASE")
    public HsqldbDatabase(HsqldbProperties properties) throws MalformedURLException {
        log(INFO, "{0}", properties);
        directory = properties.getDirectory();
        shutdownTimeout = SECONDS.toMillis(properties.getShutdownTimeoutSeconds());
        server = new Server();
        server.setNoSystemExit(true);
        server.setRestartOnShutdown(false);
        server.setPort(properties.getPort());
        server.setSilent(properties.isSilent());
        if (properties.getDatabases() == null) {
            setDatabase("NUMERICS", getClass().getResource("default.sql"));
        } else {
            for (final String database : properties.getDatabases().stringPropertyNames()) {
                final String urlText = properties.getDatabases().getProperty(database);
                if (!urlText.contains("://")) {
                    setDatabase(database, getClass().getClassLoader().getResource(urlText));
                } else {
                    setDatabase(database, new URL(urlText));
                }
            }
        }
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
        try (final Connection c = dataSource(name).getConnection()) {
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

    @MaridBeanProducer(name = "Data Source", icon = "D_DATABASE_OUTLINE")
    public DataSource dataSource(String name) {
        final int dbIndex = new ArrayList<>(databaseNameToIndex.keySet()).indexOf(name);
        final Database database = DatabaseManager.getDatabase(dbIndex);
        return new JDBCSessionDataSource(database, "PUBLIC");
    }
}
