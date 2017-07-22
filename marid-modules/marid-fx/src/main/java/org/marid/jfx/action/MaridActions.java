/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.jfx.action;

import com.google.common.collect.ComputationException;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.util.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.action.FxAction.grouped;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface MaridActions {

    static Menu[] menus(Collection<FxAction> actions) {
        return actions.stream().collect(groupingBy(a -> a.menu, TreeMap::new, toList())).entrySet().stream()
                .map(e -> {
                    final Menu menu = new Menu();
                    menu.textProperty().bind(ls(e.getKey()));
                    menu.getItems().setAll(grouped(menu.getItems(), e.getValue()));
                    return menu;
                })
                .toArray(Menu[]::new);
    }

    static void initToolbar(Collection<FxAction> actions, ToolBar toolBar) {
        final Map<String, List<FxAction>> sorted = actions.stream()
                .filter(e -> e.toolbarGroup != null)
                .collect(groupingBy(a -> a.toolbarGroup, TreeMap::new, toList()));
        sorted.values().stream()
                .flatMap(v -> concat(v.stream().map(FxAction::button), of(new Separator())))
                .forEach(toolBar.getItems()::add);
    }

    static ToolBar toolbar(Collection<FxAction> actions) {
        final ToolBar toolBar = new ToolBar();
        initToolbar(actions, toolBar);
        return toolBar;
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
