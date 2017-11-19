/*-
 * #%L
 * marid-ide
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

package org.marid.ide.logging;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.logging.Level.INFO;
import static org.marid.ide.IdePrefs.PREFERENCES;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogHandler extends Handler {

  public static final ObservableList<LogRecord> LOG_RECORDS = FXCollections.observableArrayList();

  private volatile int maxRecords;
  private final Set<Integer> blockedThreadIds = new ConcurrentSkipListSet<>();

  public IdeLogHandler() {
    maxRecords = PREFERENCES.getInt("maxLogRecords", 10_000);
  }

  public int getMaxRecords() {
    return maxRecords;
  }

  public void setMaxRecords(int maxRecords) {
    this.maxRecords = maxRecords;
  }

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      Platform.runLater(() -> {
        LOG_RECORDS.add(record);
        final int maxRecords = this.maxRecords;
        while (LOG_RECORDS.size() - maxRecords > 0) {
          LOG_RECORDS.remove(0, LOG_RECORDS.size() - maxRecords);
        }
      });
    }
  }

  @Override
  public boolean isLoggable(LogRecord record) {
    return super.isLoggable(record) && !blockedThreadIds.contains(record.getThreadID());
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
    PREFERENCES.putInt("maxLogRecords", maxRecords);
  }

  public int registerBlockedThreadId() {
    final LogRecord record = new LogRecord(INFO, null);
    blockedThreadIds.add(record.getThreadID());
    return record.getThreadID();
  }

  public void unregisterBlockedThreadId(int threadId) {
    blockedThreadIds.remove(threadId);
  }
}
