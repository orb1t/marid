package org.marid.jfx.logging;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import org.marid.jfx.icons.IconFactory;

import java.util.logging.LogRecord;

import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogComponent extends ListView<LogRecord> {

    public LogComponent(ObservableList<LogRecord> records) {
        super(records);
        setCellFactory(p -> {
            final ListCell<LogRecord> cell = new ListCell<LogRecord>() {
                @Override
                protected void updateItem(LogRecord item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        setText(m(item.getMessage(), item.getParameters()));
                        setGraphic(IconFactory.icon(item.getLevel()));
                    }
                }
            };
            cell.setFont(Font.font("Monospaced", cell.getFont().getSize() * 0.75));
            return cell;
        });
    }
}
