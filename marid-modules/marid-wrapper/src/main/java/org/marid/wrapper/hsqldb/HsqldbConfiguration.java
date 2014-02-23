/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.wrapper.hsqldb;

import org.hsqldb.server.Server;
import org.marid.wrapper.WrapperConstants;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "hsqldb")
public class HsqldbConfiguration {

    @XmlElement
    private int port = WrapperConstants.DEFAULT_WRAPPER_PORT;

    @XmlElement
    private String host = "localhost";

    @XmlElement
    private HsqldbProtocol protocol = HsqldbProtocol.HSQL;

    @XmlElement
    private boolean daemon = false;

    @XmlElement
    private boolean silent = true;

    @XmlElement
    private boolean trace = false;

    @XmlElement
    private String defaultWebPage = "index.html";

    @XmlElement
    private boolean restartOnShutdown = false;

    @XmlTransient
    private final Map<String, String> databaseMap = new LinkedHashMap<>();

    @XmlElement
    private boolean noSystemExit = false;

    public HsqldbConfiguration() {
    }

    public HsqldbConfiguration(Server server) {
        port = server.getPort();
        host = server.getAddress();
        silent = server.isSilent();
        trace = server.isTrace();
        defaultWebPage = server.getDefaultWebPage();
        restartOnShutdown = server.isRestartOnShutdown();
        noSystemExit = server.isNoSystemExit();
        protocol = HsqldbProtocol.valueOf(server.getProtocol().toUpperCase());
        for (int i = 0; i < 10; i++) {
            final String name = server.getDatabaseName(i, true);
            final String path = server.getDatabasePath(i, true);
            if (name == null || path == null) {
                break;
            } else {
                databaseMap.put(name, path);
            }
        }
    }

    @XmlTransient
    public HsqldbProtocol getProtocol() {
        return protocol;
    }

    @XmlTransient
    public String getHost() {
        return host;
    }

    @XmlTransient
    public int getPort() {
        return port;
    }

    @XmlTransient
    public boolean isDaemon() {
        return daemon;
    }

    @XmlTransient
    public boolean isSilent() {
        return silent;
    }

    @XmlTransient
    public boolean isTrace() {
        return trace;
    }

    @XmlTransient
    public String getDefaultWebPage() {
        return defaultWebPage;
    }

    @XmlTransient
    public boolean isRestartOnShutdown() {
        return restartOnShutdown;
    }

    public Map<String, String> getDatabaseMap() {
        return databaseMap;
    }

    @XmlTransient
    public boolean isNoSystemExit() {
        return noSystemExit;
    }

    @XmlElementWrapper(name = "databases")
    @XmlElement(name = "entry")
    private DbEntry[] getDbEntries() {
        return databaseMap.entrySet().stream().map(e -> new DbEntry(e.getKey(), e.getValue())).toArray(DbEntry[]::new);
    }

    public HsqldbConfiguration setPort(int port) {
        this.port = port;
        return this;
    }

    public HsqldbConfiguration setHost(String host) {
        this.host = host;
        return this;
    }

    public HsqldbConfiguration setProtocol(HsqldbProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public HsqldbConfiguration setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public HsqldbConfiguration setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }

    public HsqldbConfiguration setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public HsqldbConfiguration setDefaultWebPage(String defaultWebPage) {
        this.defaultWebPage = defaultWebPage;
        return this;
    }

    public HsqldbConfiguration setRestartOnShutdown(boolean restartOnShutdown) {
        this.restartOnShutdown = restartOnShutdown;
        return this;
    }

    private void setDbEntries(DbEntry[] entries) {
        for (final DbEntry entry : entries) {
            databaseMap.put(entry.name, entry.path);
        }
    }

    public HsqldbConfiguration setNoSystemExit(boolean noSystemExit) {
        this.noSystemExit = noSystemExit;
        return this;
    }

    public HsqldbConfiguration putDatabase(String name, String path) {
        databaseMap.put(name, path);
        return this;
    }

    private static class DbEntry {

        @XmlAttribute
        private final String name;

        @XmlAttribute
        private final String path;

        private DbEntry(String name, String path) {
            this.name = name;
            this.path = path;
        }

        private DbEntry() {
            this(null, null);
        }
    }
}
