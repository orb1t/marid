package org.hsqldb.jdbc;

import org.hsqldb.Database;
import org.hsqldb.jdbc.pool.JDBCPooledConnection;
import org.hsqldb.jdbc.pool.JDBCPooledDataSource;

import javax.sql.PooledConnection;
import java.sql.SQLException;

/**
 * @author Dmitry Ovchinnikov.
 */
public class JDBCSessionPool extends JDBCPool {

    private final Database database;
    private final String schema;

    public JDBCSessionPool(int size, Database database, String schema) {
        super(size);
        this.database = database;
        this.schema = schema;
        this.source = new JDBCPooledDataSource() {
            @Override
            public PooledConnection getPooledConnection() throws SQLException {
                return new JDBCPooledConnection(new JDBCSessionConnection(database, schema));
            }
        };
    }
}
