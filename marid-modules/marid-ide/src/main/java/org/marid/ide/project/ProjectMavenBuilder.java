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

package org.marid.ide.project;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.marid.logging.LogSupport;
import org.marid.maven.MavenProjectBuilder;
import org.marid.maven.ProjectBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder implements LogSupport {

    private final BooleanProperty buildState = new SimpleBooleanProperty(false);

    Thread build(ProjectProfile profile, Consumer<Map<String, Object>> consumer, Consumer<LogRecord> logConsumer) {
        final Thread thread = new Thread(() -> {
            Platform.runLater(() -> buildState.set(true));
            final ProjectBuilder projectBuilder = new MavenProjectBuilder(profile.getPath(), logConsumer)
                    .goals("clean", "install")
                    .profiles("conf");
            projectBuilder.build(consumer);
            Platform.runLater(() -> buildState.set(false));
        });
        thread.start();
        return thread;
    }

    @Bean
    public BooleanBinding projectDisabled(ProjectManager projectManager) {
        return projectManager.profileProperty().isNull().or(buildState);
    }
}
