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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.jfx.icons.FontIcons;
import org.marid.logging.LogSupport;
import org.marid.misc.Casts;
import org.marid.spring.AnnotatedBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Comparator.comparing;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar implements LogSupport {

    @Autowired
    public IdeToolbar(GenericApplicationContext context) {
        setMaxWidth(Double.MAX_VALUE);
        sceneProperty().addListener((observable, oldValue, newValue) -> {
            newValue.windowProperty().addListener((observable1, oldWin, newWin) -> {
                newWin.addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
                    final Map<String, Set<Node>> buttonMap = new TreeMap<>();
                    AnnotatedBean.walk(context, IdeToolbarItem.class, bean -> {
                        final IdeToolbarItem ti = bean.annotation;
                        final IdeMenuItem mi = bean.getAnnotation(IdeMenuItem.class);
                        final String group = ti.group().isEmpty() ? (mi != null ? mi.group() : ti.group()) : ti.group();
                        final String id = ti.id().isEmpty() ? null : ti.id();
                        final String tip = ti.tip().isEmpty() ? (mi != null ? mi.text() : null) : ti.tip();
                        final GlyphIcon<?> toolbarIcon = ti.icon().isEmpty() ? null : FontIcons.glyphIcon(ti.icon(), 20);
                        final GlyphIcon<?> menuIcon;
                        if (mi == null || toolbarIcon != null) {
                            menuIcon = null;
                        } else {
                            menuIcon = mi.icon().isEmpty() ? null : FontIcons.glyphIcon(mi.icon(), 20);
                        }
                        final GlyphIcon<?> icon = toolbarIcon == null ? menuIcon : toolbarIcon;
                        final Node node;
                        if (bean.object instanceof Node) {
                            node = (Node) bean.object;
                        } else if (bean.object instanceof EventHandler) {
                            final Button button = new Button(null, icon);
                            button.setOnAction(Casts.cast(bean.object));
                            node = button;
                        } else {
                            return;
                        }
                        if (node instanceof Control) {
                            final Control control = (Control) node;
                            if (tip != null) {
                                control.setTooltip(new Tooltip(tip));
                            }
                        }
                        if (id != null) {
                            node.setId(id);
                        }
                        node.setFocusTraversable(false);
                        buttonMap.computeIfAbsent(group, k -> new LinkedHashSet<>()).add(node);
                    });
                    buttonMap.forEach((group, buttons) -> {
                        buttons.stream().sorted(comparing(n -> n.getId() == null ? "" : n.getId())).forEach(getItems()::add);
                        getItems().add(new Separator());
                    });
                });
            });
        });
    }
}
