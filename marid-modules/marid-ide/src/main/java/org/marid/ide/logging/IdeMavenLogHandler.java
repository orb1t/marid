package org.marid.ide.logging;

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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeMavenLogHandler extends Handler {

    public final ObservableList<LogRecord> records = FXCollections.observableArrayList();

    public IdeMavenLogHandler(int threadId) {
        setFilter(r -> r.getThreadID() == threadId);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            Platform.runLater(() -> records.add(record));
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
