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

package org.marid.jfx.table;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.action.SpecialAction;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTableView<T> extends TableView<T> {

    protected void initialize(Initializer initializer) {
        setRowFactory(table -> {
            final TableRow<T> row = initializer.rowSupplier.get();
            row.focusedProperty().addListener((observable, oldValue, newValue) -> {
                final Map<String, FxAction> actionMap = new TreeMap<>();
                if (isFocused()) {
                    actionMap.putAll(initializer.tableActions.get());
                }
                if (newValue) {
                    actionMap.putAll(initializer.elementActions.apply(row.getItem()));
                }
                final MenuItem[] items = MaridActions.contextMenu(actionMap);
                row.setContextMenu(items.length == 0 ? null : new ContextMenu(items));
            });
            return row;
        });
        getSelectionModel().selectedItemProperty().addListener((o, oldValue, newValue) -> initializer.onSelect(newValue));
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setContextMenu(new ContextMenu(MaridActions.contextMenu(initializer.tableActions.get())));
                final T e = getSelectionModel().getSelectedItem();
                if (e == null) {
                    initializer.assign(initializer.group(initializer.tableActions.get()));
                }
            } else {
                setContextMenu(null);
                initializer.reset(Collections.emptySet());
            }
        });
    }

    protected class Initializer {

        protected final Set<SpecialAction> actions = Collections.newSetFromMap(new IdentityHashMap<>());

        protected Supplier<Map<String, FxAction>> tableActions = Collections::emptyMap;
        protected Function<T, Map<String, FxAction>> elementActions = e -> Collections.emptyMap();
        protected Supplier<TableRow<T>> rowSupplier = TableRow::new;

        public Initializer setElementActions(Function<T, Map<String, FxAction>> elementActions) {
            this.elementActions = elementActions;
            return this;
        }

        public Initializer setRowSupplier(Supplier<TableRow<T>> rowSupplier) {
            this.rowSupplier = rowSupplier;
            return this;
        }

        public Initializer setTableActions(Supplier<Map<String, FxAction>> tableActions) {
            this.tableActions = tableActions;
            return this;
        }

        protected void reset(Set<SpecialAction> set) {
            for (final SpecialAction action : actions) {
                if (!set.contains(action)) {
                    action.reset();
                    action.update();
                }
            }
        }

        protected void assign(Map<SpecialAction, Map<String, FxAction>> map) {
            map.forEach((k, v) -> {
                k.reset();
                if (v.size() == 1) {
                    k.copy(v.values().iterator().next());
                } else {
                    k.children.putAll(v);
                }
                k.update();
            });
            reset(map.keySet());
        }

        protected void onSelect(T e) {
            final Map<String, FxAction> actionMap = new TreeMap<>();
            if (isFocused()) {
                actionMap.putAll(tableActions.get());
            }
            actionMap.putAll(elementActions.apply(e));
            assign(group(actionMap));
        }

        protected Map<SpecialAction, Map<String, FxAction>> group(Map<String, FxAction> map) {
            final Map<SpecialAction, Map<String, FxAction>> specialActionMap = new IdentityHashMap<>();
            map.forEach((k, v) -> {
                if (v.specialAction != null) {
                    specialActionMap.computeIfAbsent(v.specialAction, a -> new LinkedHashMap<>()).put(k, v);
                }
            });
            return specialActionMap;
        }
    }
}
