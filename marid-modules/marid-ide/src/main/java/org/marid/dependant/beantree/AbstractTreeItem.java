/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.intellij.lang.annotations.MagicConstant;
import org.marid.ide.common.SpecialActionConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.misc.Casts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractTreeItem<T> extends TreeItem<Object> {

    private final Map<String, FxAction> specialActions = new HashMap<>();
    private final Map<String, FxAction> additionalActions = new HashMap<>();
    private final MaridContextMenu contextMenu;

    public AbstractTreeItem(T value) {
        super(value);
        contextMenu = new MaridContextMenu(m -> {
            m.getItems().clear();
            final Set<String> keys = Sets.union(specialActions.keySet(), additionalActions.keySet());
            final Map<String, FxAction> map = Maps.asMap(keys, k -> specialActions.getOrDefault(k, additionalActions.get(k)));
            m.getItems().addAll(MaridActions.contextMenu(map));
        });
    }

    public T get() {
        return Casts.cast(getValue());
    }

    protected void specialAction(@MagicConstant(valuesFromClass = SpecialActionConfiguration.class) String name, FxAction action) {
        specialActions.put(name, action);
    }

    protected void action(String name, FxAction action) {
        additionalActions.put(name, action);
    }

    public abstract ObservableValue<String> name();

    public abstract ObservableValue<Node> icon();

    public abstract ObservableValue<String> type();

    public abstract ObservableValue<String> text();

    public MaridContextMenu getContextMenu() {
        return contextMenu;
    }

    public ProjectProfile getProfile() {
        for (TreeItem<Object> item = this; item != null; item = item.getParent()) {
            if (item instanceof FileTreeItem) {
                return ((FileTreeItem) item).getProfile();
            }
        }
        throw new IllegalStateException("No root");
    }
}
