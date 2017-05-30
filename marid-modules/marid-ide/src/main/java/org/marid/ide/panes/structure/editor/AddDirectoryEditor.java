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

package org.marid.ide.panes.structure.editor;

import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import org.marid.ide.project.ProjectProfile;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.LocalizedStrings.fls;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AddDirectoryEditor extends AbstractFileEditor {

    public AddDirectoryEditor() {
        super(Files::isDirectory);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Add a directory";
    }

    @Nonnull
    @Override
    public Node getIcon() {
        return new HBox(5, glyphIcon("M_FOLDER", 16), glyphIcon("M_ADD_CIRCLE", 16));
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Override
    public void edit(@Nonnull ProjectProfile profile, @Nonnull Path file) {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.contentTextProperty().bind(fls("%s:", "Directory name"));
        dialog.titleProperty().bind(ls("Add a directory"));
        final Optional<String> optionalDirectoryName = dialog.showAndWait();
        if (optionalDirectoryName.isPresent()) {
            final String dirName = optionalDirectoryName.get();
            final Path path = file.resolve(dirName);
            try {
                Files.createDirectory(path);
            } catch (Exception x) {
                n(WARNING, "Unable to create a directory {0}", x, path);
            }
        }
    }
}
