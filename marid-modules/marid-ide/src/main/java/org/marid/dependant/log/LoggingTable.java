package org.marid.dependant.log;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.track.Tracks;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class LoggingTable extends TableView<LogRecord> {

    @Autowired
    public LoggingTable(LoggingFilter loggingFilter) {
        super(loggingFilter.filteredList(IdeLogHandler.LOG_RECORDS));
        setTableMenuButtonVisible(true);
        final String columnDefaultStyle = "-fx-font-size: smaller";
        getColumns().add(levelColumn());
        getColumns().add(timestampColumn());
        getColumns().add(messageColumn());
        getColumns().add(loggerColumn());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setStyle("-fx-font-size: small; -fx-focus-color: transparent;");
        getColumns().forEach(c -> {
            final String oldStyle = c.getStyle();
            c.setStyle(oldStyle != null ? oldStyle + columnDefaultStyle : columnDefaultStyle);
        });
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Tracks.track(this, getItems(), getSelectionModel());
    }

    public static IconDescriptor icon(Level level) {
        switch (level.intValue()) {
            case Integer.MAX_VALUE:
                return new IconDescriptor("D_SELECT_OFF", "red");
            case Integer.MIN_VALUE:
                return new IconDescriptor("D_ARROW_ALL", "green");
            case 1000:
                return new IconDescriptor("M_ERROR", "red");
            case 900:
                return new IconDescriptor("F_WARNING", "orange");
            case 800:
                return new IconDescriptor("F_INFO_CIRCLE", "blue");
            case 700:
                return new IconDescriptor("M_CONTROL_POINT", "green");
            case 500:
                return new IconDescriptor("D_BATTERY_60", "green");
            case 400:
                return new IconDescriptor("D_BATTERY_80", "green");
            case 300:
                return new IconDescriptor("D_BATTERY_CHARGING_100", "green");
            default:
                return new IconDescriptor("D_BATTERY_UNKNOWN", "gray");
        }
    }

    private TableColumn<LogRecord, IconDescriptor> levelColumn() {
        final TableColumn<LogRecord, IconDescriptor> col = new TableColumn<>();
        col.setText("☼");
        col.setCellValueFactory(param -> new SimpleObjectProperty<>(icon(param.getValue().getLevel())));
        col.setStyle("-fx-alignment: center;");
        col.setCellFactory(c -> new TableCell<LogRecord, IconDescriptor>() {
            @Override
            protected void updateItem(IconDescriptor item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item.getGlyphIcon());
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
        final TableColumn<LogRecord, String> col = new TableColumn<>(L10n.s("Time"));
        col.setCellValueFactory(param -> {
            final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            final String time = format.format(new Date(param.getValue().getMillis()));
            return new SimpleStringProperty(time);
        });
        col.setMinWidth(60);
        col.setPrefWidth(60);
        col.setMaxWidth(100);
        col.setSortable(false);
        return col;
    }

    private TableColumn<LogRecord, String> loggerColumn() {
        final TableColumn<LogRecord, String> col = new TableColumn<>(L10n.s("Logger"));
        col.setCellValueFactory(param -> {
            final String loggerName = String.valueOf(param.getValue().getLoggerName());
            final String[] parts = loggerName.split("[.]");
            for (int i = 0; i < parts.length - 1; i++) {
                final String part = parts[i];
                parts[i] = part.length() >= 2 ? part.substring(0, 1) : part;
            }
            return new SimpleStringProperty(String.join(".", parts));
        });
        col.setStyle("-fx-alignment: center-right;");
        col.setMinWidth(310);
        col.setPrefWidth(320);
        col.setMaxWidth(350);
        col.setSortable(false);
        return col;
    }

    private TableColumn<LogRecord, String> messageColumn() {
        final TableColumn<LogRecord, String> col = new TableColumn<>(L10n.s("Message"));
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
        col.setMinWidth(300);
        col.setPrefWidth(400);
        col.setMaxWidth(Double.MAX_VALUE);
        col.setSortable(false);
        return col;
    }

    @PreDestroy
    private void destroy() {
        setItems(FXCollections.emptyObservableList());
    }

    public static class IconDescriptor {

        public final String icon;
        public final String css;

        private IconDescriptor(String icon, String css) {
            this.icon = icon;
            this.css = css;
        }

        private Node getGlyphIcon() {
            final Node glyphIcon = FontIcons.glyphIcon(icon, 16);
            glyphIcon.setStyle("-fx-fill: " + css);
            return glyphIcon;
        }
    }
}
