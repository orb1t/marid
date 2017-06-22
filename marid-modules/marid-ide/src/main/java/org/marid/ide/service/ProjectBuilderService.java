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

import javafx.scene.control.ContextMenu;
import javafx.scene.layout.HBox;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.status.IdeService;
import org.marid.maven.MavenProjectBuilder;
import org.marid.maven.ProjectBuilder;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectBuilderService extends IdeService<HBox> {

    private final IdeLogHandler logHandler;
    private final ProjectProfile profile;

    @Autowired
    public ProjectBuilderService(IdeLogHandler logHandler, ProjectManager projectManager) {
        this.logHandler = logHandler;
        this.profile = projectManager.getProfile();

        setOnRunning(event -> profile.enabledProperty().set(false));
        setOnFailed(event -> profile.enabledProperty().set(true));
        setOnSucceeded(event -> profile.enabledProperty().set(true));
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
            projectBuilder.build(result -> {

                if (!result.exceptions.isEmpty()) {
                    final IllegalStateException thrown = new IllegalStateException("Maven build error");
                    result.exceptions.forEach(thrown::addSuppressed);
                    throw thrown;
                }
            });
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
