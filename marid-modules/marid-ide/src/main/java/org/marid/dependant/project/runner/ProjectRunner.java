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

package org.marid.dependant.project.runner;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectRunner extends Stage implements LogSupport {

    @Autowired
    public ProjectRunner(ProjectRunnerPane runnerPane) {
        getIcons().addAll(Ide.primaryStage.getIcons());
        initModality(Modality.NONE);
        setScene(new Scene(runnerPane, 800, 600));
        setMaximized(true);
        setTitle("[" + runnerPane.getProfile() + "]");
        setOnCloseRequest(event -> {
            boolean stopped = false;
            try {
                runnerPane.printStream.println("exit");
                stopped = runnerPane.process.waitFor(10L, TimeUnit.SECONDS);
            } catch (Exception x) {
                log(WARNING, "Unable to stop the process", x);
            } finally {
                if (!stopped) {
                    try {
                        runnerPane.processManager.close();
                    } catch (Exception x) {
                        log(WARNING, "Unable to destroy the process", x);
                    }
                }
            }
        });
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
