/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.jfx.logging;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.marid.jfx.icons.IconFactory;

import java.util.logging.LogRecord;

import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogComponent extends ListView<LogRecord> {

    public LogComponent(ObservableList<LogRecord> records) {
        super(records);
        setStyle("-fx-font-size: 80%; -fx-font-family: monospace");
        setCellFactory(p -> new ListCell<LogRecord>() {
            @Override
            protected void updateItem(LogRecord item, boolean empty) {
                final Runnable runnable = () -> {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        setText(m(item.getMessage(), item.getParameters()));
                        setGraphic(IconFactory.icon(item.getLevel()));
                    }
                };
                if (Platform.isFxApplicationThread()) {
                    runnable.run();
                } else {
                    Platform.runLater(runnable);
                }
            }
        });
    }
}
