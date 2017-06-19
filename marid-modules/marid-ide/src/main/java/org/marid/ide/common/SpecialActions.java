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

import javafx.scene.control.SelectionModel;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public <T> void setup(@Nonnull SelectionModel<T> selectionModel,
                          @Nonnull Function<T, Map<String, FxAction>> actions) {
        selectionModel.selectedItemProperty().addListener((o, oV, nV) -> {
            final Map<String, FxAction> map = actions.apply(nV);
            final Map<SpecialAction, Map<String, FxAction>> specialActions = new IdentityHashMap<>();
            map.forEach((k, v) -> {
                if (v.specialAction != null) {
                    specialActions.computeIfAbsent(v.specialAction, key -> new LinkedHashMap<>()).put(k, v);
                }
            });
            specialActions.forEach((k, v) -> {
                if (v.size() == 1) {
                    final FxAction action = v.values().iterator().next();
                    k.copy(action);
                    k.update();
                } else {
                    k.reset();
                    k.children.putAll(v);
                    k.update();
                }
            });
            actionMap.values().stream().filter(v -> !specialActions.containsKey(v)).forEach(a -> {
                a.reset();
                a.update();
            });
        });
    }
}
