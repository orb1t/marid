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

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.marid.ide.logging.IdeLogHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.logging.LogRecord;

import static javafx.geometry.Orientation.VERTICAL;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeStatusBar extends HBox {

    private final Label label = new Label();

    @Autowired
    public IdeStatusBar(IdeStatusProfile profile, IdeLogHandler logHandler) {
        super(5);
        setAlignment(Pos.CENTER_LEFT);
        setHgrow(label, Priority.SOMETIMES);
        getChildren().addAll(label, new Separator(VERTICAL), profile, new Separator(VERTICAL));
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        setHgrow(label, Priority.SOMETIMES);
        label.textProperty().bind(Bindings.createObjectBinding(() -> {
            final ObservableList<LogRecord> records = logHandler.getLogRecords();
            if (records.isEmpty()) {
                return null;
            } else {
                final LogRecord record = records.get(records.size() - 1);
                return m(record.getMessage(), record.getParameters());
            }
        }, logHandler.getLogRecords()));
        setPadding(new Insets(5));
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        final Observable[] observables = getChildren().stream()
                .skip(1L)
                .filter(Control.class::isInstance)
                .map(Control.class::cast)
                .map(Region::widthProperty)
                .toArray(Observable[]::new);
        final Callable<Double> width = () -> getChildren().stream()
                .skip(1L)
                .mapToDouble(c -> c.prefWidth(getHeight()))
                .reduce(getWidth() - getPadding().getLeft() - getPadding().getRight(), (a, e) -> a - e - 5);
        label.maxWidthProperty().bind(Bindings.createDoubleBinding(width, observables));
    }
}
