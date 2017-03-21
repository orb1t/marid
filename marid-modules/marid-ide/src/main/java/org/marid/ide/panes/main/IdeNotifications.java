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

package org.marid.ide.panes.main;

import javafx.application.Platform;
import org.controlsfx.control.Notifications;
import org.marid.Ide;
import org.marid.ide.logging.IdeLogHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.Comparator.comparingInt;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
@Lazy(false)
public class IdeNotifications  {

    private final Map<Level, LogRecord> recordMap = new ConcurrentSkipListMap<>(comparingInt(Level::intValue));

    @Autowired
    private void init(IdeLogHandler handler) {
        handler.addRecordCosnumer(logRecords -> logRecords.forEach(r -> recordMap.put(r.getLevel(), r)));
    }

    @Scheduled(fixedDelay = 1_000L)
    private void update() {
        recordMap.entrySet().removeIf(e -> {
            final Level level = e.getKey();
            final LogRecord record = e.getValue();
            final String text = m(Locale.getDefault(), record.getMessage(), record.getParameters());
            final Notifications notifications = Notifications.create()
                    .text(text)
                    .title(s(level.getName()))
                    .darkStyle();
            Platform.runLater(() -> {
                notifications.owner(Ide.primaryStage);
                switch (level.getName()) {
                    case "INFO":
                        notifications.showInformation();
                        break;
                    case "ERROR":
                    case "SEVERE":
                        notifications.showError();
                        break;
                    case "WARNING":
                        notifications.showWarning();
                        break;
                    case "CONFIG":
                        notifications.showConfirm();
                        break;
                    default:
                        notifications.show();
                        break;
                }
            });
            return true;
        });
    }
}
