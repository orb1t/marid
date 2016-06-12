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
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.marid.jfx.icons.FontIcons;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.spring.AnnotatedBean;
import org.marid.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeMenu extends MenuBar implements L10nSupport, LogSupport {

    @Autowired
    public IdeMenu(Stage primaryStage, GenericApplicationContext context) {
        setMaxWidth(Double.MAX_VALUE);
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
            final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
            AnnotatedBean.walk(context, IdeMenuItem.class, bean -> {
                final IdeMenuItem mi = bean.annotation;
                final GlyphIcon<?> icon = mi.icon().isEmpty() ? null : FontIcons.glyphIcon(mi.icon(), 16);
                final String key = mi.key().isEmpty() ? null : mi.key();
                final String text = mi.text();
                final MenuItem menuItem;
                if (bean.object instanceof MenuItem) {
                    menuItem = (MenuItem) bean.object;
                } else if (bean.object instanceof EventHandler) {
                    menuItem = mi.type().createItem(s(text), icon);
                    menuItem.setOnAction(Utils.cast(bean.object));
                } else {
                    return;
                }
                if (key != null) {
                    menuItem.setAccelerator(KeyCombination.valueOf(key));
                }
                itemMap
                        .computeIfAbsent(mi.menu(), k -> new TreeMap<>())
                        .computeIfAbsent(mi.group(), k -> new TreeMap<>())
                        .put(mi.text(), menuItem);
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
    }
}
