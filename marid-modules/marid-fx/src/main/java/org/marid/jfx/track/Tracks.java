package org.marid.jfx.track;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SelectionModel;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Tracks {

    static <T> void track(Control control, ObservableList<T> list, SelectionModel<T> selectionModel) {
        control.getProperties().put("TRACK_SELECTION", true);
        if (!list.isEmpty()) {
            selectionModel.clearAndSelect(list.size() - 1);
        }
        selectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == list.size() - 1) {
                control.getProperties().replace("TRACK_SELECTION", false, true);
            } else {
                control.getProperties().replace("TRACK_SELECTION", true, false);
            }
        });
        list.addListener((ListChangeListener.Change<? extends T> c) -> {
            while (c.next()) {
                if (Boolean.TRUE.equals(control.getProperties().get("TRACK_SELECTION"))) {
                    if (!list.isEmpty()) {
                        final int row = list.size() - 1;
                        control.fireEvent(new ScrollToEvent<>(control, control, ScrollToEvent.scrollToTopIndex(), row));
                        selectionModel.clearAndSelect(row);
                    }
                }
            }
        });
    }
}
