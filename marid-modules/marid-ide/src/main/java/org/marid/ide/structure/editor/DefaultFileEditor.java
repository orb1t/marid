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

import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.awt.Desktop.Action.OPEN;
import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class DefaultFileEditor extends AbstractFileEditor<Desktop> {

    public DefaultFileEditor() {
        super(Files::isRegularFile);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Open a file in a default editor";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("M_OPEN_IN_BROWSER");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Nullable
    @Override
    protected Desktop editorContext(@Nonnull Path path) {
        return isDesktopSupported() && getDesktop().isSupported(OPEN) ? getDesktop() : null;
    }

    @Override
    protected void edit(@Nonnull Path path, @Nonnull Desktop context) {
        EventQueue.invokeLater(() -> {
            try {
                context.open(path.toFile());
            } catch (Exception e) {
                n(WARNING, "Unable to edit {0}", e, path);
            }
        });
    }
}
