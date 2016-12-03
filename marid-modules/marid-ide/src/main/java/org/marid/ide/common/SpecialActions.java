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

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.jfx.menu.MaridMenu;
import org.marid.misc.UnionMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static org.marid.misc.Iterables.last;

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

    public ContextMenu contextMenu(Supplier<Map<String, FxAction>> additionalItemsSupplier) {
        return new MaridContextMenu(m -> {
            final ObservableList<MenuItem> items = m.getItems();
            items.clear();
            final MaridMenu maridMenu = new MaridMenu(new UnionMap<>(actionMap, additionalItemsSupplier.get()));
            for (final Menu menu : maridMenu.getMenus()) {
                items.addAll(menu.getItems());
                items.add(new SeparatorMenuItem());
            }
            last(items).filter(SeparatorMenuItem.class::isInstance).ifPresent(items::remove);
        });
    }
}
