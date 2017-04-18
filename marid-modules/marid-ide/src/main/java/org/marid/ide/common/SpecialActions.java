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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.menu.MaridContextMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SpecialActions {

    private final Map<String, FxAction> actionMap;

    @Autowired
    public SpecialActions(@Qualifier("specialAction") Map<String, FxAction> actionMap) {
        this.actionMap = actionMap;
    }

    public FxAction getAction(@MagicConstant(valuesFromClass = SpecialActionConfiguration.class) String name) {
        return actionMap.get(name);
    }

    public MaridContextMenu contextMenu(Supplier<Map<String, FxAction>> additionalItemsSupplier) {
        return new MaridContextMenu(m -> {
            m.getItems().clear();
            final Map<String, FxAction> add = additionalItemsSupplier.get();
            final Set<String> keys = Sets.union(actionMap.keySet(), add.keySet());
            final Map<String, FxAction> map = Maps.asMap(keys, k -> actionMap.getOrDefault(k, add.get(k)));
            m.getItems().addAll(MaridActions.contextMenu(map));
        });
    }
}
