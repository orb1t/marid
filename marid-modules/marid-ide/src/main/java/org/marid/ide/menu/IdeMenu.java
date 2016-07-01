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

package org.marid.ide.menu;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcons;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.spring.action.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeMenu extends MenuBar implements L10nSupport, LogSupport {

    @Autowired
    public IdeMenu(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        setMaxWidth(Double.MAX_VALUE);
        sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.windowProperty().addListener((observable1, oldWin, newWin) -> {
                    newWin.addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
                        final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
                        menuActionsFactory.getObject().forEach((id, action) -> {
                            if (action.getGroup() == null) {
                                return;
                            }
                            final GlyphIcon<?> icon = action.getIcon() != null ? FontIcons.glyphIcon(action.getIcon(), 16) : null;
                            final MenuItem menuItem;
                            if (action.selectedProperty() != null) {
                                final CheckMenuItem checkMenuItem = new CheckMenuItem(s(action.getText()), icon);
                                checkMenuItem.selectedProperty().bindBidirectional(action.selectedProperty());
                                menuItem = checkMenuItem;
                            } else {
                                menuItem = new MenuItem(s(action.getText()), icon);
                            }
                            menuItem.setAccelerator(action.getAccelerator());
                            menuItem.setOnAction(action.getEventHandler());
                            itemMap
                                    .computeIfAbsent(action.getMenu(), k -> new TreeMap<>())
                                    .computeIfAbsent(action.getGroup(), k -> new TreeMap<>())
                                    .put(action.getText(), menuItem);
                        });
                        itemMap.forEach((menu, groupMap) -> {
                            final Menu m = new Menu(s(menu));
                            groupMap.forEach((group, menuItems) -> {
                                m.getItems().addAll(menuItems.values());
                                m.getItems().add(new SeparatorMenuItem());
                            });
                            if (!m.getItems().isEmpty()) {
                                m.getItems().remove(m.getItems().size() - 1);
                            }
                            getMenus().add(m);
                        });
                    });
                });
            }
        });
    }
}
