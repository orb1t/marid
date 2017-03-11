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

package org.marid.jfx.action;

import com.google.common.collect.ComputationException;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface MaridActions {

    static Menu[] menus(Map<String, FxAction> actionMap) {
        final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
        actionMap.forEach((id, action) -> {
            if (action.getMenu() == null) {
                return;
            }
            final MenuItem menuItem;
            if (action.selectedProperty() != null) {
                final CheckMenuItem checkMenuItem = new CheckMenuItem();
                checkMenuItem.selectedProperty().bindBidirectional(action.selectedProperty());
                menuItem = checkMenuItem;
            } else if (action.getChildren() != null) {
                final Menu menu = new Menu();
                final Menu[] subMenus = menus(action.getChildren());
                switch (subMenus.length) {
                    case 0:
                        return;
                    case 1:
                        menu.getItems().addAll(subMenus[0].getItems());
                        break;
                    default:
                        menu.getItems().addAll(subMenus);
                        break;
                }
                menuItem = menu;
            } else {
                menuItem = new MenuItem();
            }

            if (action.textProperty() != null) {
                menuItem.textProperty().bind(action.textProperty());
            }
            if (action.iconProperty() != null) {
                menuItem.graphicProperty().bind(createObjectBinding(() -> {
                    final String icon = action.getIcon();
                    return icon != null ? glyphIcon(icon, 16) : null;
                }, action.iconProperty()));
            }
            if (action.acceleratorProperty() != null) {
                menuItem.acceleratorProperty().bind(action.acceleratorProperty());
            }

            if (!(menuItem instanceof Menu)) {
                menuItem.setOnAction(event -> action.getEventHandler().handle(event));
                if (action.disabledProperty() != null) {
                    menuItem.disableProperty().bindBidirectional(action.disabledProperty());
                }
            }
            itemMap
                    .computeIfAbsent(action.getMenu(), k -> new TreeMap<>())
                    .computeIfAbsent(action.getGroup(), k -> new TreeMap<>())
                    .put(id, menuItem);
        });
        final List<Menu> menus = new ArrayList<>();
        itemMap.forEach((menu, groupMap) -> {
            final Menu m = new Menu();
            m.textProperty().bind(ls(menu));
            groupMap.forEach((group, menuItems) -> {
                m.getItems().addAll(menuItems.values());
                m.getItems().add(new SeparatorMenuItem());
            });
            if (!m.getItems().isEmpty()) {
                m.getItems().remove(m.getItems().size() - 1);
            }
            menus.add(m);
        });
        return menus.toArray(new Menu[menus.size()]);
    }

    static MenuItem[] contextMenu(Map<String, FxAction> actionMap) {
        final AtomicBoolean first = new AtomicBoolean(true);
        return Stream.of(menus(actionMap))
                .flatMap(m -> first.compareAndSet(true, false)
                        ? m.getItems().stream()
                        : concat(of(new SeparatorMenuItem()), m.getItems().stream()))
                .toArray(MenuItem[]::new);
    }

    static Node[] toolbar(Map<String, FxAction> actionMap) {
        final Map<String, Map<String, FxAction>> sorted = actionMap.entrySet().stream()
                .filter(e -> e.getValue().getToolbarGroup() != null)
                .collect(groupingBy(
                        e -> e.getValue().getToolbarGroup(),
                        TreeMap::new,
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (a1, a2) -> a2, TreeMap::new))
                );
        return sorted.values().stream()
                .flatMap(v -> concat(v.values().stream()
                        .map(a -> {
                            final Button button = new Button();
                            button.setFocusTraversable(false);
                            button.setOnAction(event -> a.getEventHandler().handle(event));
                            if (a.disabledProperty() != null) {
                                button.disableProperty().bindBidirectional(a.disabledProperty());
                            }
                            if (a.iconProperty() != null) {
                                button.graphicProperty().bind(createObjectBinding(
                                        () -> glyphIcon(a.getIcon(), 20),
                                        a.iconProperty())
                                );
                            }
                            final StringProperty hint = a.hintProperty() != null ? a.hintProperty() : a.textProperty();
                            if (hint != null) {
                                final Tooltip tooltip = new Tooltip();
                                tooltip.textProperty().bind(hint);
                                button.setTooltip(tooltip);
                            }
                            return button;
                        }), of(new Separator())))
                .toArray(Node[]::new);
    }

    static <T> T execute(Callable<T> task, long timeout, TimeUnit timeUnit) throws UncheckedTimeoutException {
        if (Platform.isFxApplicationThread()) {
            try {
                return task.call();
            } catch (Exception x) {
                throw new ComputationException(x);
            }
        } else {
            final SynchronousQueue<Pair<T, Throwable>> queue = new SynchronousQueue<>();
            Platform.runLater(() -> {
                try {
                    final T result;
                    try {
                        result = task.call();
                    } catch (Throwable x) {
                        queue.put(new Pair<>(null, x));
                        return;
                    }
                    queue.put(new Pair<>(result, null));
                } catch (InterruptedException x) {
                    queue.offer(new Pair<>(null, x));
                }
            });
            final Pair<T, Throwable> pair;
            try {
                pair = queue.poll(timeout, timeUnit);
            } catch (InterruptedException x) {
                throw new UncheckedTimeoutException("Interrupted", x);
            }
            if (pair == null) {
                throw new UncheckedTimeoutException("Timeout exceeded");
            } else if (pair.getKey() != null) {
                return pair.getKey();
            } else if (pair.getValue() instanceof RuntimeException) {
                throw (RuntimeException) pair.getValue();
            } else if (pair.getValue() instanceof Exception) {
                throw new ComputationException(pair.getValue());
            } else if (pair.getValue() instanceof Error) {
                throw (Error) pair.getValue();
            } else {
                throw new IllegalStateException("Unknown error", pair.getValue());
            }
        }
    }

    static <T> T execute(Callable<T> task) {
        return execute(task, 1L, TimeUnit.MINUTES);
    }
}
