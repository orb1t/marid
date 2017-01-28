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

package org.marid.ide.status;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeStatusBar extends GridPane {

    @Autowired
    public IdeStatusBar(IdeStatusProfile profile, IdeLogHandler logHandler, IdeIndicators indicators) {
        setHgap(5);
        final Label label = new Label();
        addRow(0, label, separator(), profile, separator(), indicators);
        setHgrow(label, Priority.SOMETIMES);
        label.textProperty().bind(Bindings.createObjectBinding(() -> {
            final ObservableList<LogRecord> records = logHandler.getLogRecords();
            if (records.isEmpty()) {
                return null;
            } else {
                final LogRecord record = records.get(records.size() - 1);
                return L10n.m(record.getMessage(), record.getParameters());
            }
        }, logHandler.getLogRecords()));
        setPadding(new Insets(5));
    }

    private Separator separator() {
        return new Separator(Orientation.VERTICAL);
    }
}
