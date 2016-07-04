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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import org.marid.l10n.L10n;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class MenuContainerBuilder {

    private final Map<String, Set<MenuBuilder.MenuItemBuilder>> actionMap = new LinkedHashMap<>();

    public MenuContainerBuilder menu(String text, boolean toolbar, Consumer<MenuBuilder> menuBuilderConsumer) {
        menuBuilderConsumer.accept(new MenuBuilder(text, toolbar));
        return this;
    }

    public void build(Consumer<Menu> menuConsumer, Consumer<Node> nodeConsumer) {
        actionMap.forEach((menuText, actions) -> {
            final Menu menu = new Menu(L10n.s(menuText));
            menuConsumer.accept(menu);
            actions.forEach(action -> {
                final MenuItem menuItem;
                if (!action.menuItems.isEmpty()) {
                    final Menu subMenu = new Menu();
                    menuItem = subMenu;
                    subMenu.getItems().addAll(action.menuItems);
                } else if (action.separator) {
                    menuItem = new SeparatorMenuItem();
                } else {
                    menuItem = new MenuItem();
                    menuItem.setOnAction(action.action);
                }
                if (action.icon != null) {
                    menuItem.graphicProperty().bind(Bindings.createObjectBinding(
                            () -> glyphIcon(action.icon.getValue(), 16),
                            action.icon
                    ));
                }
                if (action.text != null) {
                    menuItem.textProperty().bind(createStringBinding(() -> L10n.s(action.text.get()), action.text));
                }
                if (action.accelerator != null) {
                    menuItem.acceleratorProperty().bind(action.accelerator);
                }
                if (action.disabled != null) {
                    menuItem.disableProperty().bind(action.disabled);
                }
                menu.getItems().add(menuItem);
            });
            final Set<MenuBuilder.MenuItemBuilder> set = actions.stream()
                    .filter(a -> a.toolbar)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!set.isEmpty()) {
                set.forEach(action -> {
                    final Button button = new Button();
                    button.setFocusTraversable(false);
                    if (action.icon != null) {
                        button.graphicProperty().bind(Bindings.createObjectBinding(
                                () -> glyphIcon(action.icon.getValue(), 20),
                                action.icon
                        ));
                    }
                    if (action.text != null) {
                        final Tooltip tooltip = new Tooltip();
                        tooltip.textProperty().bind(createStringBinding(() -> L10n.s(action.text.get()), action.text));
                        button.setTooltip(tooltip);
                    }
                    if (action.disabled != null) {
                        button.disableProperty().bind(action.disabled);
                    }
                    nodeConsumer.accept(button);
                    if (!action.menuItems.isEmpty()) {
                        button.setOnAction(event -> {
                            final ContextMenu contextMenu = new ContextMenu();
                            contextMenu.getItems().addAll(action.menuItems);
                            contextMenu.show(button, Side.BOTTOM, 0, 0);
                        });
                    } else {
                        button.setOnAction(action.action);
                    }
                });
                nodeConsumer.accept(new Separator());
            }
        });
    }

    public class MenuBuilder {

        private final boolean toolbar;
        private final Set<MenuItemBuilder> menuItemBuilders;

        private MenuBuilder(String menu, boolean toolbar) {
            this.toolbar = toolbar;
            this.menuItemBuilders = actionMap.computeIfAbsent(menu, k -> new LinkedHashSet<>());
        }

        public MenuBuilder item(boolean toolbar, Consumer<MenuItemBuilder> menuItemBuilderConsumer) {
            final MenuItemBuilder menuItemBuilder = new MenuItemBuilder(toolbar, false);
            menuItemBuilders.add(menuItemBuilder);
            menuItemBuilderConsumer.accept(menuItemBuilder);
            return this;
        }

        public MenuBuilder item(Consumer<MenuItemBuilder> menuItemBuilderConsumer) {
            return item(toolbar, menuItemBuilderConsumer);
        }

        public MenuBuilder item(String text, boolean toolbar, Consumer<MenuItemBuilder> menuItemBuilderConsumer) {
            final MenuItemBuilder menuItemBuilder = new MenuItemBuilder(toolbar, false).text(text);
            menuItemBuilders.add(menuItemBuilder);
            menuItemBuilderConsumer.accept(menuItemBuilder);
            return this;
        }

        public MenuBuilder item(String text, Consumer<MenuItemBuilder> menuItemBuilderConsumer) {
            return item(text, toolbar, menuItemBuilderConsumer);
        }

        public MenuBuilder separator() {
            final MenuItemBuilder menuItemBuilder = new MenuItemBuilder(false, true);
            menuItemBuilders.add(menuItemBuilder);
            return this;
        }

        public final class MenuItemBuilder {

            private final boolean toolbar;
            private final boolean separator;
            private final Set<MenuItem> menuItems = new LinkedHashSet<>();

            private ObservableStringValue text;
            private ObservableBooleanValue disabled;
            private ObservableValue<KeyCombination> accelerator;
            private ObservableStringValue icon;
            private EventHandler<ActionEvent> action;

            private MenuItemBuilder(boolean toolbar, boolean separator) {
                this.toolbar = toolbar;
                this.separator = separator;
            }

            public MenuItemBuilder text(ObservableStringValue text) {
                this.text = text;
                return this;
            }

            public MenuItemBuilder text(String text) {
                return text(new SimpleStringProperty(text));
            }

            public MenuItemBuilder disabled(ObservableBooleanValue disabled) {
                this.disabled = disabled;
                return this;
            }

            public MenuItemBuilder disabled(boolean disabled) {
                return disabled(new SimpleBooleanProperty(disabled));
            }

            public MenuItemBuilder accelerator(ObservableValue<KeyCombination> accelerator) {
                this.accelerator = accelerator;
                return this;
            }

            public MenuItemBuilder accelerator(KeyCombination accelerator) {
                return accelerator(new SimpleObjectProperty<>(accelerator));
            }

            public MenuItemBuilder accelerator(String accelerator) {
                return accelerator(KeyCombination.valueOf(accelerator));
            }

            public MenuItemBuilder menuItems(MenuItem... menuItems) {
                Collections.addAll(this.menuItems, menuItems);
                return this;
            }

            public MenuItemBuilder menuItems(Collection<? extends MenuItem> menuItems) {
                this.menuItems.addAll(menuItems);
                return this;
            }

            public MenuItemBuilder icon(ObservableStringValue icon) {
                this.icon = icon;
                return this;
            }

            public MenuItemBuilder icon(String icon) {
                return icon(new SimpleStringProperty(icon));
            }

            public MenuItemBuilder action(EventHandler<ActionEvent> action) {
                this.action = action;
                return this;
            }
        }
    }
}
