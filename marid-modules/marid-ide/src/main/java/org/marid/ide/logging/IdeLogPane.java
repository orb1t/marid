/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.ide.logging;

import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.stage.WindowEvent;
import org.marid.Ide;
import org.marid.jfx.panes.MaridScrollPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Component
public class IdeLogPane extends BorderPane {

    @Autowired
    public void initCenter(IdeLogView logView, GenericApplicationContext context) {
        final MaridScrollPane scrollPane = new MaridScrollPane(logView);
        setCenter(scrollPane);
        context.addApplicationListener(new ApplicationListener<ContextStartedEvent>() {
            @Override
            public void onApplicationEvent(ContextStartedEvent event) {
                context.getApplicationListeners().remove(this);
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
