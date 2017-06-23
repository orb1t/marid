/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.service;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.logging.IdeMavenLogHandler;
import org.marid.ide.panes.main.IdeStatusBar;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.status.IdeService;
import org.marid.jfx.logging.LogComponent;
import org.marid.maven.MavenProjectBuilder;
import org.marid.maven.ProjectBuilder;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectBuilderService extends IdeService<HBox> {

    private final IdeLogHandler logHandler;
    private final IdeStatusBar statusBar;

    private ProjectProfile profile;

    @Autowired
    public ProjectBuilderService(IdeLogHandler logHandler, IdeStatusBar statusBar) {
        this.logHandler = logHandler;
        this.statusBar = statusBar;
    }

    public ProjectBuilderService setProfile(ProjectProfile profile) {
        this.profile = profile;
        setOnRunning(event -> profile.enabledProperty().set(false));
        setOnFailed(event -> profile.enabledProperty().set(true));
        setOnSucceeded(event -> profile.enabledProperty().set(true));
        return this;
    }

    @Override
    protected BuilderTask createTask() {
        return new BuilderTask();
    }

    private class BuilderTask extends IdeTask {

        private BuilderTask() {
            updateTitle(profile.getName());
        }

        @Override
        protected void execute() throws Exception {
            final ProjectBuilder projectBuilder = new MavenProjectBuilder(profile.getPath())
                    .goals("clean", "install")
                    .profiles("conf");
            final int threadId = logHandler.registerBlockedThreadId();
            final IdeMavenLogHandler mavenLogHandler = new IdeMavenLogHandler(threadId);
            final Logger root = Logger.getLogger("");
            root.addHandler(mavenLogHandler);
            updateGraphic(box -> {
                final LogComponent logComponent = new LogComponent(mavenLogHandler.records);
                final BorderPane pane = new BorderPane(logComponent);
                BorderPane.setMargin(logComponent, new Insets(5));
                logComponent.setPrefSize(800, 600);
                statusBar.addNotification(Bindings.format("%s: %s", profile.getName(), ls("Maven Build")), pane);
            });
            try {
                projectBuilder.build(result -> {
                    if (!result.exceptions.isEmpty()) {
                        final IllegalStateException thrown = new IllegalStateException("Maven build error");
                        result.exceptions.forEach(thrown::addSuppressed);
                        throw thrown;
                    }
                });
            } finally {
                logHandler.unregisterBlockedThreadId(threadId);
                root.removeHandler(mavenLogHandler);
            }
        }

        @Nonnull
        @Override
        protected HBox createGraphic() {
            return new HBox(IdeShapes.circle(profile.hashCode(), 16));
        }

        @Override
        protected ContextMenu contextMenu() {
            return new ContextMenu();
        }
    }
}
