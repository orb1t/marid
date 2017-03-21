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

package org.marid.ide.logging;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.marid.IdePrefs;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogHandler extends Handler {

    private final ConcurrentLinkedQueue<LogRecord> queue = new ConcurrentLinkedQueue<>();
    private final ObservableList<LogRecord> logRecords = observableArrayList();
    private final List<Consumer<List<LogRecord>>> recordConsumers = new CopyOnWriteArrayList<>();

    public int getMaxLogRecords() {
        return IdePrefs.PREFERENCES.getInt("maxLogRecords", 10_000);
    }

    public void setMaxLogRecords(int maxLogRecords) {
        IdePrefs.PREFERENCES.putInt("maxLogRecords", maxLogRecords);
    }

    @Override
    public void publish(LogRecord record) {
        queue.add(record);
    }

    public ObservableList<LogRecord> getLogRecords() {
        return logRecords;
    }

    public void addRecordCosnumer(Consumer<List<LogRecord>> logRecordsConsumer) {
        recordConsumers.add(logRecordsConsumer);
    }

    public void removeRecordCosnumer(Consumer<List<LogRecord>> logRecordsConsumer) {
        recordConsumers.remove(logRecordsConsumer);
    }

    @Override
    @Scheduled(fixedDelay = 100L)
    public void flush() {
        if (!queue.isEmpty()) {
            final List<LogRecord> records = new ArrayList<>();
            queue.removeIf(records::add);
            recordConsumers.forEach(c -> c.accept(records));
            final int maxLogRecords = getMaxLogRecords();
            Platform.runLater(() -> {
                logRecords.addAll(records);
                final int size = logRecords.size();
                final int toRemove = size - maxLogRecords;
                if (toRemove > 0) {
                    logRecords.remove(0, toRemove);
                }
            });
        }
    }

    @Override
    public void close() {
        recordConsumers.clear();
    }

    public static GlyphIcon<?> logIcon(Level level, int size) {
        switch (level.getName()) {
            case "WARNING":
                return FontIcons.glyphIcon(FontIcon.F_WARNING, size);
            case "ERROR":
            case "SEVERE":
                return FontIcons.glyphIcon(FontIcon.M_ERROR, size);
            case "INFO":
                return FontIcons.glyphIcon(FontIcon.M_INFO, size);
            case "CONFIG":
                return FontIcons.glyphIcon(FontIcon.M_ADJUST, size);
            default:
                return FontIcons.glyphIcon(FontIcon.M_FACE, size);
        }
    }
}
