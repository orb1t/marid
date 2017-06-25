package org.marid.ide.logging;

import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.stage.WindowEvent;
import org.marid.Ide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class IdeLogPane extends BorderPane {

    @Autowired
    public void initCenter(IdeLogView logView, ApplicationEventMulticaster multicaster) {
        setCenter(logView);
        multicaster.addApplicationListener(new ApplicationListener<ContextStartedEvent>() {
            @Override
            public void onApplicationEvent(ContextStartedEvent event) {
                multicaster.removeApplicationListener(this);
                Ide.primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        Ide.primaryStage.removeEventHandler(WindowEvent.WINDOW_SHOWN, this);
                        logView.scrollTo(logView.getItems().size() - 1);
                    }
                });
            }
        });
    }
}
