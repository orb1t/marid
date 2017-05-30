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
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcons;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class DefaultFileEditor extends AbstractFileEditor {

    public DefaultFileEditor() {
        super(Files::isRegularFile, Files::isWritable, Files::isReadable);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Open file in OS-specific editor";
    }

    @Nonnull
    @Override
    public Node getIcon() {
        return FontIcons.glyphIcon("M_OPEN_WITH", 16);
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Override
    protected boolean isEditable(@Nonnull Path path, @Nonnull ProjectProfile profile) {
        final AtomicBoolean desktopSupported = new AtomicBoolean();
        try {
            EventQueue.invokeAndWait(() -> desktopSupported.set(Desktop.isDesktopSupported()));
        } catch (Exception x) {
            return false;
        }
        return desktopSupported.get();
    }

    @Override
    public void edit(@Nonnull ProjectProfile profile, @Nonnull Path file) {
        try {
            EventQueue.invokeLater(() -> {
                try {
                    Desktop.getDesktop().edit(file.toFile());
                } catch (Exception x) {
                    n(WARNING, "Unable to edit file {0}", x, file);
                }
            });
        } catch (Exception x) {
            n(WARNING, "Unable to edit file {0}", x, file);
        }
    }
}
