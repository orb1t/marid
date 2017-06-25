package org.marid.ide.logging;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
