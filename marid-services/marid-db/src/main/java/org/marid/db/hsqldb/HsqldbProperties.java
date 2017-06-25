package org.marid.db.hsqldb;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class HsqldbProperties {

    private File directory = new File("daqDatabase");
    private long shutdownTimeoutSeconds = 60L;
    private Properties databases;
    private int port = 9001;
    private boolean silent = true;

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

    public Properties getDatabases() {
        return databases;
    }

    public void setDatabases(Properties databases) {
        this.databases = databases;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
