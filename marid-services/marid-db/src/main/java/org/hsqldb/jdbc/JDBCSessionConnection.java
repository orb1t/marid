package org.hsqldb.jdbc;

import org.hsqldb.Database;

import javax.annotation.Nonnull;
import java.sql.SQLException;

import static java.util.logging.Level.SEVERE;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
public class JDBCSessionConnection extends JDBCConnection {

    public JDBCSessionConnection(@Nonnull Database database, @Nonnull String schema) throws SQLException {
        super(database.getSessionManager().newSysSession());
        setSchema(schema);
    }

    @Override
    public synchronized void close() throws SQLException {
        try {
            sessionProxy.close();
        } catch (Throwable x) {
            log(SEVERE, "Unable to close session id={0}", x, sessionProxy.getId());
        } finally {
            sessionProxy = null;
        }
    }
}
