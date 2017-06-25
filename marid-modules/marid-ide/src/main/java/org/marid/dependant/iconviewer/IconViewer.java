package org.marid.dependant.iconviewer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IconViewer extends Stage {

    @Autowired
    public IconViewer(IconViewerTable table, IdePane idePane) {
        super(StageStyle.UNIFIED);
        initOwner(idePane.getScene().getWindow());
        setTitle(L10n.s("Icon viewer"));
        final BorderPane pane = new BorderPane(table);
        pane.setPrefSize(800, 600);
        final Label countLabel = new Label(L10n.s("Icon count: %d", table.getItems().size()));
        pane.setBottom(countLabel);
        BorderPane.setMargin(table, new Insets(10, 10, 5, 10));
        BorderPane.setMargin(countLabel, new Insets(5, 10, 10, 10));
        setScene(new Scene(pane));
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
