package org.marid.ide.configurations;

import javafx.application.Platform;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;


/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ApplicationConfiguration {

    @IdeAction
    public FxAction exitAction() {
        return new FxAction("x", "Application")
                .bindText("Exit")
                .setIcon("D_EXIT_TO_APP")
                .setEventHandler(event -> Platform.exit());
    }
}
