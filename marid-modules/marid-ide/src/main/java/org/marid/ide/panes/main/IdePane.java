package org.marid.ide.panes.main;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class IdePane extends BorderPane {

    @Autowired
    private void center(IdeSplitPane pane) {
        setCenter(pane);
    }

    @Autowired
    private void top(IdeMenu ideMenu, IdeToolbar toolbar) {
        final BorderPane menuPane = new BorderPane();
        menuPane.setCenter(ideMenu);
        setTop(new VBox(menuPane, toolbar));
    }

    @Autowired
    private void bottom(IdeStatusBar statusBar) {
        setBottom(statusBar);
    }
}
