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

import org.marid.beans.MaridBean;
import org.marid.misc.Calls;

import java.io.File;
import java.net.URI;

/**
 * @author Dmitry Ovchinnikov.
 */
@MaridBean(icon = "M.DATA_USAGE")
public final class HsqldbProperties {

    private File directory = new File("daqDatabase");
    private long shutdownTimeoutSeconds = 60L;
    private URI sqlDirectoryUri = Calls.call(() -> getClass().getResource("numerics.sql").toURI().resolve("."));
    private int connectionPoolSize = 10;

    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public long getShutdownTimeoutSeconds() {
        return shutdownTimeoutSeconds;
    }

    public void setShutdownTimeoutSeconds(long shutdownTimeoutSeconds) {
        this.shutdownTimeoutSeconds = shutdownTimeoutSeconds;
    }

    public URI getSqlDirectoryUri() {
        return sqlDirectoryUri;
    }

    public void setSqlDirectoryUri(URI sqlDirectoryUri) {
        this.sqlDirectoryUri = sqlDirectoryUri;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }
}