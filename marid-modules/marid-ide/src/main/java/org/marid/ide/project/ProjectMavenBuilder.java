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
import org.marid.ide.logging.IdeLogHandler;
import org.marid.maven.MavenBuildResult;
import org.marid.maven.MavenProjectBuilder;
import org.marid.maven.ProjectBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectMavenBuilder {

    private final IdeLogHandler ideLogHandler;

    @Autowired
    public ProjectMavenBuilder(IdeLogHandler ideLogHandler) {
        this.ideLogHandler = ideLogHandler;
    }

    void build(ProjectProfile profile, Configuration configuration) {
        final Thread thread = new Thread(() -> {
            Platform.runLater(() -> profile.enabledProperty().set(false));
            final ProjectBuilder projectBuilder = new MavenProjectBuilder(profile.getPath())
                    .goals("clean", "install")
                    .profiles("conf");
            try {
                final LogRecord logRecord = new LogRecord(Level.INFO, null);
                final int threshold = configuration.level.intValue();
                final int threadId = logRecord.getThreadID();
                ideLogHandler.setFilter(r -> r.getThreadID() != threadId || r.getLevel().intValue() >= threshold);
                projectBuilder.build(configuration.resultConsumer);
            } finally {
                ideLogHandler.setFilter(null);
            }
            Platform.runLater(() -> profile.enabledProperty().set(true));
        });
        thread.start();
    }

    public static final class Configuration {

        private Consumer<MavenBuildResult> resultConsumer = r -> {};
        private Level level = Level.INFO;

        public Consumer<MavenBuildResult> getResultConsumer() {
            return resultConsumer;
        }

        public Configuration setResultConsumer(Consumer<MavenBuildResult> resultConsumer) {
            this.resultConsumer = resultConsumer;
            return this;
        }

        public Level getLevel() {
            return level;
        }

        public Configuration setLevel(Level level) {
            this.level = level;
            return this;
        }
    }
}
