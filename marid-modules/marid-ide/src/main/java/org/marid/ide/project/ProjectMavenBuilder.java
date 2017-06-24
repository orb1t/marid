/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
