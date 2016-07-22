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
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.IdePrefs;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectRunner extends Stage implements LogSupport {

    private final Preferences preferences;

    @Autowired
    public ProjectRunner(ProjectRunnerPane runnerPane) {
        preferences = IdePrefs.PREFERENCES.node(runnerPane.profile.getName()).node("runner");
        getIcons().addAll(Ide.primaryStage.getIcons());
        setScene(new Scene(runnerPane, preferences.getDouble("width", 800), preferences.getDouble("height", 600)));
        setTitle("[" + runnerPane.getProfile() + "]");
        setOnCloseRequest(event -> {
            preferences.putDouble("x", getX());
            preferences.putDouble("y", getY());
            preferences.putDouble("width", getWidth());
            preferences.putDouble("height", getHeight());
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
        setOnShowing(event -> {
            setX(preferences.getDouble("x", getX()));
            setY(preferences.getDouble("y", getY()));
        });
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
