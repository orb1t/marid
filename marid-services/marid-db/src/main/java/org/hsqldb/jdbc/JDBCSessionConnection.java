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

package org.hsqldb.jdbc;

import org.hsqldb.Database;
import org.hsqldb.SessionInterface;

import javax.annotation.Nonnull;
import java.sql.SQLException;

/**
 * @author Dmitry Ovchinnikov.
 */
public class JDBCSessionConnection extends JDBCConnection {

    public JDBCSessionConnection(@Nonnull Database database, @Nonnull String schema) throws SQLException {
        this(database.getSessionManager().newSysSession(), schema);
    }

    public JDBCSessionConnection(@Nonnull SessionInterface c, @Nonnull String schema) throws SQLException {
        super(c);
        setSchema(schema);
    }

    @Override
    public synchronized void close() throws SQLException {
        try {
            sessionProxy.close();
        } catch (Throwable x) {
            // ignore
        } finally {
            sessionProxy = null;
        }
    }
}
