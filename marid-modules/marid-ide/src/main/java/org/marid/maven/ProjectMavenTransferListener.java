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

package org.marid.maven;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.logging.Level.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectMavenTransferListener implements TransferListener {

    private final Consumer<LogRecord> logRecordConsumer;

    public ProjectMavenTransferListener(Consumer<LogRecord> logRecordConsumer) {
        this.logRecordConsumer = logRecordConsumer;
    }

    private void log(Level level, String message, Object... args) {
        final LogRecord logRecord = new LogRecord(level, message);
        logRecord.setSourceClassName(null);
        logRecord.setParameters(args);
        logRecordConsumer.accept(logRecord);
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        //log(INFO, "{0}", event);
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        log(WARNING, "{0}", event);
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        log(INFO, "{0}", event);
    }

    @Override
    public void transferFailed(TransferEvent event) {
        log(SEVERE, "{0}", event);
    }
}
