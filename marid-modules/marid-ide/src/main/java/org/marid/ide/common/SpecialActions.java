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

package org.marid.ide.common;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.util.Pair;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SpecialActions {

    private final Map<String, SpecialAction> actionMap;

    @Autowired
    public SpecialActions(@Qualifier("specialAction") Map<String, SpecialAction> actionMap) {
        this.actionMap = actionMap;
    }

    public void forEach(BiConsumer<String, FxAction> consumer) {
        actionMap.forEach(consumer);
    }

    public FxAction get(String name) {
        return actionMap.get(name);
    }

    public <T extends Control> FxActions<T> actions(@Nullable FxActions<?> parent,
                                                    @Nonnull T control,
                                                    @Nonnull Function<T, Map<String, FxAction>> actions) {
        return new FxActions<>(parent, control, actions);
    }

    public <T extends Control> FxActions<T> actions(@Nonnull T control,
                                                    @Nonnull Function<T, Map<String, FxAction>> actions) {
        return actions(null, control, actions);
    }

    public final class FxActions<T extends Control> {

        private final FxActions<?> parent;
        private final T control;
        private final Function<T, Map<String, FxAction>> actions;

        private FxActions(@Nullable FxActions<?> parent,
                          @Nonnull T control,
                          @Nonnull Function<T, Map<String, FxAction>> actions) {
            this.parent = parent;
            this.control = control;
            this.actions = actions;
        }

        public Map<String, FxAction> actions() {
            return actions.apply(control);
        }

        private Stream<Map<String, FxAction>> actionMapStream() {
            return Stream.concat(parent == null ? Stream.empty() : parent.actionMapStream(), Stream.of(actions()));
        }

        public void setup() {
            control.focusedProperty().addListener((observable, oldValue, newValue) -> {
                actionMap.values().forEach(SpecialAction::reset);
                control.setContextMenu(null);
                if (newValue) {
                    final Map<String, FxAction> map = actionMapStream()
                            .flatMap(m -> m.entrySet().stream())
                            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2, TreeMap::new));
                    map.entrySet().stream()
                            .map(e -> new Pair<>(actionMap.get(e.getKey()), e.getValue()))
                            .filter(e -> e.getKey() != null)
                            .forEach(e -> e.getKey().copy(e.getValue()));
                    control.setContextMenu(new ContextMenu(MaridActions.contextMenu(map)));
                }
            });
        }
    }
}
