package org.hsqldb.jdbc;

import org.hsqldb.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dmitry Ovchinnikov.
 */
public class JDBCSessionDataSource extends JDBCDataSource {

    private final Database database;
    private final String schema;

    public JDBCSessionDataSource(Database database, String schema) {
        this.database = database;
        this.schema = schema;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new JDBCSessionConnection(database, schema);
    }
}
