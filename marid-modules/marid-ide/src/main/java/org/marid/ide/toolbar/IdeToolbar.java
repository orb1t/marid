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

package org.marid.ide.toolbar;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.stage.WindowEvent;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcons;
import org.marid.l10n.L10n;
import org.marid.logging.LogSupport;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Comparator.comparing;
import static javafx.beans.binding.Bindings.createObjectBinding;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar implements LogSupport {

    @Autowired
    public IdeToolbar(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        setMaxWidth(Double.MAX_VALUE);
        sceneProperty().addListener((observable, oldValue, newValue) -> {
            newValue.windowProperty().addListener((observable1, oldWin, newWin) -> {
                newWin.addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
                    final Map<String, Set<Node>> buttonMap = new TreeMap<>();
                    final Map<Node, String> reversedMap = new IdentityHashMap<>();
                    menuActionsFactory.getObject().forEach((id, action) -> {
                        if (action.getToolbarGroup() == null) {
                            return;
                        }
                        final String group = action.getToolbarGroup();
                        final GlyphIcon<?> icon = action.getIcon() != null ? FontIcons.glyphIcon(action.getIcon(), 20) : null;
                        final Button button = new Button(null, icon);
                        button.setFocusTraversable(false);
                        button.setOnAction(action.getEventHandler());
                        if (action.disabledProperty() != null) {
                            button.disableProperty().bindBidirectional(action.disabledProperty());
                        }
                        if (action.hintProperty() != null) {
                            button.tooltipProperty().bind(createObjectBinding(() -> new Tooltip(L10n.s(action.getHint())), action.hintProperty()));
                        } else if (action.textProperty() != null) {
                            button.tooltipProperty().bind(createObjectBinding(() -> new Tooltip(L10n.s(action.getText())), action.textProperty()));
                        }
                        reversedMap.put(button, id);
                        buttonMap.computeIfAbsent(group, g -> new HashSet<>()).add(button);
                    });
                    buttonMap.forEach((group, buttons) -> {
                        buttons.stream().sorted(comparing(reversedMap::get)).forEach(getItems()::add);
                        getItems().add(new Separator());
                    });
                });
            });
        });
    }
}
