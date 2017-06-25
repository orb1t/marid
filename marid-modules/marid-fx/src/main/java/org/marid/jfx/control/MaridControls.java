package org.marid.jfx.control;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridControls {

    static ScrollPane createMaridScrollPane(Node node) {
        final ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent");
        return scrollPane;
    }
}
