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

package org.marid.ide.panes.logging;

import com.google.common.collect.ImmutableMap;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.ide.icons.IdeIcons;
import org.marid.l10n.L10nSupport;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class LoggingTable extends TableView<LogRecord> implements L10nSupport {

    private final Map<Level, GlyphIcons> iconMap = ImmutableMap.<Level, GlyphIcons>builder()
            .put(Level.INFO, FontAwesomeIcon.INFO)
            .put(Level.WARNING, FontAwesomeIcon.WARNING)
            .put(Level.SEVERE, MaterialIcon.ERROR)
            .put(Level.CONFIG, MaterialDesignIcon.PACKAGE)
            .put(Level.FINE, MaterialDesignIcon.BATTERY_60)
            .put(Level.FINER, MaterialDesignIcon.BATTERY_80)
            .put(Level.FINEST, MaterialDesignIcon.BATTERY_CHARGING_100)
            .build();

    public LoggingTable() {
        super(ideLogHandler().getLogRecords());
        getColumns().add(levelColumn());
        getColumns().add(timestampColumn());
        getColumns().add(loggerColumn());
        getColumns().add(messageColumn());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setStyle("-fx-font-size: small");
    }

    private static IdeLogHandler ideLogHandler() {
        for (final Handler handler : Logger.getLogger("").getHandlers()) {
            if (handler instanceof IdeLogHandler) {
                return (IdeLogHandler) handler;
            }
        }
        throw new NoSuchElementException(IdeLogHandler.class.getSimpleName());
    }

    private GlyphIcons iconByLevel(Level level) {
        return iconMap.getOrDefault(level, MaterialDesignIcon.BATTERY_UNKNOWN);
    }

    private TableColumn<LogRecord, GlyphIcons> levelColumn() {
        final TableColumn<LogRecord, GlyphIcons> col = new TableColumn<>();
        col.setGraphic(IdeIcons.glyphIcon(FontAwesomeIcon.SHIELD));
        col.setCellValueFactory(param -> new SimpleObjectProperty<>(iconByLevel(param.getValue().getLevel())));
        col.setStyle("-fx-font-size: small; -fx-alignment: center");
        col.setCellFactory(c -> new TableCell<LogRecord, GlyphIcons>() {
            @Override
            protected void updateItem(GlyphIcons item, boolean empty) {
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(IdeIcons.glyphIcon(item));
                }
            }
        });
        col.setMinWidth(25);
        col.setPrefWidth(30);
        col.setMaxWidth(40);
        col.setSortable(false);
        return col;
    }

    private TableColumn<LogRecord, String> timestampColumn() {
        final TableColumn<LogRecord, String> col = new TableColumn<>(s("Time"));
        col.setCellValueFactory(param -> {
            final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            final String time = format.format(new Date(param.getValue().getMillis()));
            return new SimpleStringProperty(time);
        });
        col.setStyle("-fx-font-size: small");
        col.setMinWidth(60);
        col.setPrefWidth(60);
        col.setMaxWidth(100);
        col.setSortable(false);
        return col;
    }

    private TableColumn<LogRecord, String> loggerColumn() {
        final TableColumn<LogRecord, String> col = new TableColumn<>(s("Logger"));
        col.setCellValueFactory(param -> {
            final String loggerName = String.valueOf(param.getValue().getLoggerName());
            final String[] parts = loggerName.split("[.]");
            for (int i = 0; i < parts.length - 1; i++) {
                final String part = parts[i];
                parts[i] = part.length() >= 2 ? part.substring(0, 1) : part;
            }
            return new SimpleStringProperty(String.join(".", parts));
        });
        col.setStyle("-fx-font-size: small; -fx-alignment: center-right");
        col.setMinWidth(150);
        col.setPrefWidth(200);
        col.setMaxWidth(250);
        col.setSortable(false);
        return col;
    }

    private TableColumn<LogRecord, String> messageColumn() {
        final TableColumn<LogRecord, String> col = new TableColumn<>(s("Message"));
        col.setCellValueFactory(param -> {
            String message = param.getValue().getMessage();
            if (param.getValue().getParameters() != null && param.getValue().getParameters().length > 0) {
                try {
                    message = MessageFormat.format(message, param.getValue().getParameters());
                } catch (Exception x) {
                    // ignore
                }
            }
            return new SimpleStringProperty(message);
        });
        col.setStyle("-fx-font-size: small");
        col.setMinWidth(300);
        col.setPrefWidth(400);
        col.setMaxWidth(Double.MAX_VALUE);
        col.setSortable(false);
        return col;
    }
}
