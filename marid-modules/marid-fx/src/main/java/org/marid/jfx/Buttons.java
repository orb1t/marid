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

package org.marid.jfx;

import de.jensd.fx.glyphs.GlyphIcons;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.marid.jfx.icons.FontIcons;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Buttons {

    static Button toolButton(String text, String tooltip, GlyphIcons icon, EventHandler<ActionEvent> eventHandler) {
        final Button button = new Button(text, icon != null ? FontIcons.glyphIcon(icon) : null);
        if (tooltip != null) {
            button.setTooltip(new Tooltip(tooltip));
        }
        button.setOnAction(eventHandler);
        button.setFocusTraversable(false);
        return button;
    }
}
