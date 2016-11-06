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

package org.marid.jfx.toolbar;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.marid.jfx.action.FxAction;

import java.util.*;

import static java.util.Comparator.comparing;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridToolbar extends ToolBar {

    public MaridToolbar() {
        setMaxWidth(Double.MAX_VALUE);
    }

    public MaridToolbar(Map<String, FxAction> actionMap) {
        this();
        init(actionMap);
    }

    public void init(Map<String, FxAction> actionMap) {
        final Map<String, Set<Node>> buttonMap = new TreeMap<>();
        final Map<Node, String> reversedMap = new IdentityHashMap<>();
        actionMap.forEach((id, a) -> {
            if (a.getToolbarGroup() == null) {
                return;
            }
            final String group = a.getToolbarGroup();
            final GlyphIcon<?> icon = a.getIcon() != null ? glyphIcon(a.getIcon(), 20) : null;
            final Button button = new Button(null, icon);
            button.setFocusTraversable(false);
            button.setOnAction(event -> a.getEventHandler().handle(event));
            if (a.disabledProperty() != null) {
                button.disableProperty().bindBidirectional(a.disabledProperty());
            }
            final StringProperty hintText = a.hintProperty() != null ? a.hintProperty() : a.textProperty();
            if (hintText != null) {
                final Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bind(hintText);
                button.setTooltip(tooltip);
            }
            reversedMap.put(button, id);
            buttonMap.computeIfAbsent(group, g -> new HashSet<>()).add(button);
        });
        buttonMap.forEach((group, buttons) -> {
            buttons.stream().sorted(comparing(reversedMap::get)).forEach(getItems()::add);
            getItems().add(new Separator());
        });
    }
}
