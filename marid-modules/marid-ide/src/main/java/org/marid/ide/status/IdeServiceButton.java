package org.marid.ide.status;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeServiceButton extends Button {

    final Label label = new Label();
    final HBox box = new HBox(5, label);

    public IdeServiceButton() {
        setGraphic(box);

        HBox.setHgrow(label, Priority.ALWAYS);

        label.setMaxHeight(Double.MAX_VALUE);
        box.setMaxHeight(Double.MAX_VALUE);
    }
}
