package org.marid.ide.logging;

import javafx.collections.FXCollections;
import org.marid.jfx.logging.LogComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeLogView extends LogComponent {

    @Autowired
    public IdeLogView() {
        super(FXCollections.emptyObservableList());
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        setItems(IdeLogHandler.LOG_RECORDS);
    }
}
