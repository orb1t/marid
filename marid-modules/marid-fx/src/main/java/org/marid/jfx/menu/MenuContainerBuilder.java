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

package org.marid.jfx.menu;

import de.jensd.fx.glyphs.GlyphIcons;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.action.Action;
import org.marid.jfx.icons.FontIcons;
import org.marid.l10n.L10nSupport;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class MenuContainerBuilder implements L10nSupport {

    private final Map<String, Set<Action>> actionMap = new LinkedHashMap<>();
    private final Map<String, Set<Action>> toolbarActionMap = new LinkedHashMap<>();

    public MenuContainerBuilder menu(String text, Consumer<MenuBuilder> menuBuilderConsumer) {
        menuBuilderConsumer.accept(new MenuBuilder(text));
        return this;
    }

    public void build(Consumer<Menu> menuConsumer, Consumer<Node> nodeConsumer) {
        actionMap.forEach((menuText, actions) -> {
            final Menu menu = new Menu(s(menuText));
            menuConsumer.accept(menu);
            actions.forEach(action -> {
                final MenuItem menuItem;
                if (action.getProperties().containsKey("menu")) {
                    final Menu subMenu = new Menu();
                    subMenu.textProperty().bindBidirectional(action.textProperty());
                    menuItem = subMenu;
                    subMenu.getItems().addAll(((ContextMenu) action.getProperties().get("menu")).getItems());
                } else if ("-".equals(action.getText())) {
                    menuItem = new SeparatorMenuItem();
                } else {
                    menuItem = new MenuItem();
                    menuItem.textProperty().bindBidirectional(action.textProperty());
                }
                if (action.getProperties().containsKey("icon")) {
                    menuItem.setGraphic(FontIcons.glyphIcon((GlyphIcons) action.getProperties().get("icon"), 16));
                }
                menuItem.disableProperty().bindBidirectional(action.disabledProperty());
                menuItem.acceleratorProperty().bindBidirectional(action.acceleratorProperty());
                menuItem.setOnAction(action);
                menu.getItems().add(menuItem);
            });
        });
        toolbarActionMap.values().forEach(actions -> {
            actions.forEach(action -> {
                final Button button = new Button();
                button.setFocusTraversable(false);
                button.setGraphic(FontIcons.glyphIcon((GlyphIcons) action.getProperties().get("icon"), 20));
                final Tooltip tooltip = new Tooltip();
                tooltip.textProperty().bindBidirectional(action.textProperty());
                button.setTooltip(tooltip);
                button.disableProperty().bindBidirectional(action.disabledProperty());
                nodeConsumer.accept(button);
                if (action.getProperties().containsKey("menu")) {
                    button.setOnAction(event -> {
                        final ContextMenu contextMenu = (ContextMenu) action.getProperties().get("menu");
                        contextMenu.show(button, Side.BOTTOM, 0, 0);
                    });
                } else {
                    button.setOnAction(action);
                }
            });
            nodeConsumer.accept(new Separator());
        });
    }

    public class MenuBuilder {

        private final String menu;

        private MenuBuilder(String menu) {
            this.menu = menu;
        }

        public MenuBuilder item(String label, GlyphIcons icon, String accelerator, Consumer<ActionEvent> eventHandler) {
            final boolean toolbar = label.startsWith("*");
            if (toolbar) {
                label = label.substring(1);
            }
            final Set<Action> actions = actionMap.computeIfAbsent(menu, l -> new LinkedHashSet<>());
            final Action action = new Action(LS.s(label), eventHandler);
            if (icon != null) {
                action.getProperties().put("icon", icon);
            }
            if (accelerator != null) {
                action.setAccelerator(KeyCombination.valueOf(accelerator));
            }
            actions.add(action);
            if (toolbar) {
                toolbarActionMap.computeIfAbsent(menu, k -> new LinkedHashSet<>()).add(action);
            }
            return this;
        }

        public MenuBuilder item(String label, GlyphIcons icon, Consumer<ActionEvent> eventHandler) {
            return item(label, icon, null, eventHandler);
        }

        public MenuBuilder item(String label, GlyphIcons icon, ContextMenu contextMenu) {
            item(label, icon, null, null);
            return last(a -> a.getProperties().put("menu", contextMenu));
        }

        public MenuBuilder last(Consumer<Action> actionConsumer) {
            final Set<Action> actions = actionMap.get(menu);
            if (actions != null) {
                actionConsumer.accept(new LinkedList<>(actions).getLast());
            }
            return this;
        }

        public MenuBuilder separator() {
            final Set<Action> actions = actionMap.computeIfAbsent(menu, l -> new LinkedHashSet<>());
            actions.add(new Action("-"));
            return this;
        }
    }
}
