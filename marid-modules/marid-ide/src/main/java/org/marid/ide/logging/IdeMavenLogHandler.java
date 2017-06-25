package org.marid.ide.logging;

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
