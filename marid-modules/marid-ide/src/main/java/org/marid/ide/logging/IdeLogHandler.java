/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
