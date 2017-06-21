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

import org.marid.ide.common.Directories;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class RemoveProfileEditor extends AbstractFileEditor<Path> {

    private final ProjectManager projectManager;

    @Autowired
    public RemoveProfileEditor(Directories directories, ProjectManager projectManager) {
        super(p -> Files.isDirectory(p) && p.getParent().equals(directories.getProfiles()));
        this.projectManager = projectManager;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Remove profile";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("D_FOLDER_REMOVE");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "profile";
    }

    @Nullable
    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }

    @Override
    protected void edit(@Nonnull Path path, @Nonnull Path context) {
        projectManager.getProfiles().stream()
                .filter(p -> p.getName().equals(context.getFileName().toString()))
                .findFirst()
                .ifPresent(projectManager::remove);
    }
}
