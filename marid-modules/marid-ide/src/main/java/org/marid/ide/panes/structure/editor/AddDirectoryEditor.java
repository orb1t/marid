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
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AddDirectoryEditor extends AbstractFileEditor<Path> {

    public AddDirectoryEditor() {
        super(Files::isDirectory);
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull Path context) {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(s("Add a directory"));
        dialog.setContentText(s("Directory name") + ":");
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
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }
}
