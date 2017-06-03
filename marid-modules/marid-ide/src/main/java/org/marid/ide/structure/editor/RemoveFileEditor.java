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

package org.marid.ide.structure.editor;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class RemoveFileEditor extends AbstractFileEditor<Path> {

    @Autowired
    public RemoveFileEditor(ProjectManager projectManager) {
        super(path -> Stream.concat(
                Stream.of(projectManager.getProfilesDir()),
                projectManager.getProfiles().stream().map(ProjectProfile::getPath)
        ).noneMatch(path::equals));
    }

    @Nonnull
    @Override
    public String getName() {
        return "Remove file";
    }

    @Nonnull
    @Override
    public Node getIcon() {
        return new HBox(5, glyphIcon("M_FOLDER", 16), glyphIcon("M_REMOVE_CIRCLE", 16));
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull Path path) {
        try {
            Files.delete(file);
        } catch (Exception x) {
            n(WARNING, "Unable to delete {0}", x, file);
        }
    }

    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }
}
