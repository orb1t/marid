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

import javafx.collections.ObservableList;
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
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTableView<T> extends TableView<T> {

    public MaridTableView(ObservableList<T> list) {
        super(list);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    protected void initialize(Initializer initializer) {
        setRowFactory(table -> {
            final TableRow<T> row = initializer.rowSupplier.get();
            row.focusedProperty().addListener((observable, oldValue, newValue) -> {
                final List<FxAction> actions = new ArrayList<>();
                if (isFocused()) actions.addAll(initializer.tableActions.get());
                if (newValue) actions.addAll(initializer.elementActions.apply(row.getItem()));
                final MenuItem[] items = MaridActions.contextMenu(actions);
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

    public class Initializer {

        protected final Set<SpecialAction> actions = Collections.newSetFromMap(new IdentityHashMap<>());

        protected Supplier<Collection<FxAction>> tableActions = Collections::emptyList;
        protected Function<T, Collection<FxAction>> elementActions = e -> Collections.emptyList();
        protected Supplier<TableRow<T>> rowSupplier = TableRow::new;

        public Initializer setElementActions(Function<T, Collection<FxAction>> elementActions) {
            this.elementActions = elementActions;
            return this;
        }

        public Initializer setRowSupplier(Supplier<TableRow<T>> rowSupplier) {
            this.rowSupplier = rowSupplier;
            return this;
        }

        public Initializer setTableActions(Supplier<Collection<FxAction>> tableActions) {
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

        protected void assign(Map<SpecialAction, Collection<FxAction>> map) {
            map.forEach((k, v) -> {
                k.reset();
                if (v.size() == 1) {
                    k.copy(v.iterator().next());
                } else {
                    k.children.addAll(v);
                }
                k.update();
            });
            reset(map.keySet());
        }

        protected void onSelect(T e) {
            final Collection<FxAction> actions = new ArrayList<>();
            if (isFocused()) {
                actions.addAll(tableActions.get());
            }
            actions.addAll(elementActions.apply(e));
            assign(group(actions));
        }

        protected Map<SpecialAction, Collection<FxAction>> group(Collection<FxAction> actions) {
            return actions.stream()
                    .filter(v -> v.specialAction != null)
                    .collect(Collectors.groupingBy(v -> v.specialAction, Collectors.toCollection(ArrayList::new)));
        }
    }
}
