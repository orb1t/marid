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

package org.marid.dependant.iconviewer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.marid.Ide;
import org.marid.ide.menu.IdeMenuItem;

import static org.marid.jfx.icons.FontIcon.D_IMAGE_ALBUM;

/**
 * @author Dmitry Ovchinnikov
 */
public class IconViewerManager {

    @IdeMenuItem(menu = "Icons", text = "Open viewer...", group = "icons", icon = D_IMAGE_ALBUM)
    public EventHandler<ActionEvent> showViewer() {
        return event -> {
            final IconViewer iconViewer = Ide.newWindow(IconViewer.class);
            iconViewer.show();
        };
    }
}
