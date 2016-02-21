/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.project;

import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.logging.AbstractLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectPlexusLogger extends AbstractLogger {

    private final Map<String, ProjectPlexusLogger> children = new ConcurrentHashMap<>();
    private final Consumer<LogRecord> logRecordConsumer;

    public ProjectPlexusLogger(String name, Consumer<LogRecord> logRecordConsumer) {
        super(MavenExecutionRequest.LOGGING_LEVEL_INFO, name);
        this.logRecordConsumer = logRecordConsumer;
    }

    @Override
    public void debug(String message, Throwable throwable) {
        log(Level.FINE, message, throwable);
    }

    @Override
    public void info(String message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log(Level.WARNING, message, throwable);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(Level.SEVERE, message, throwable);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        log(Level.SEVERE, message, throwable);
    }

    private void log(Level level, String message, Throwable throwable) {
        final LogRecord logRecord = new LogRecord(level, message);
        logRecord.setThrown(throwable);
        logRecord.setLoggerName(getName());
        logRecord.setSourceClassName(null);
        logRecordConsumer.accept(logRecord);
    }

    @Override
    public ProjectPlexusLogger getChildLogger(String name) {
        return children.computeIfAbsent(name, k -> new ProjectPlexusLogger(name, logRecordConsumer));
    }
}
