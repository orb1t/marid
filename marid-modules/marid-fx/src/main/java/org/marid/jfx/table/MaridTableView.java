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
import org.marid.jfx.action.SpecialActions;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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

    @Resource
    public void setSpecialActions(SpecialActions specialActions) {
        getProperties().put("#specialActions", specialActions);
    }

    public SpecialActions getSpecialActions() {
        return (SpecialActions) getProperties().get("#specialActions");
    }

    protected void initialize(Initializer initializer) {
        setRowFactory(table -> {
            final TableRow<T> row = initializer.rowSupplier.get();
            row.selectedProperty().addListener((o, oV, nV) -> {
                if (!isFocused()) {
                    return;
                }
                if (nV) {
                    final Collection<FxAction> actions = initializer.tableActions.apply(row.getItem());
                    final MenuItem[] items = MaridActions.contextMenu(actions);
                    row.setContextMenu(items.length == 0 ? null : new ContextMenu(items));
                    getSpecialActions().assign(initializer.group(actions));
                } else {
                    row.setContextMenu(null);
                    getSpecialActions().reset();
                }
            });
            return row;
        });
        focusedProperty().addListener((o, oV, nV) -> {
            if (nV) {
                final Collection<FxAction> actions = initializer.tableActions.apply(null);
                getSpecialActions().assign(initializer.group(actions));
                setContextMenu(new ContextMenu(MaridActions.contextMenu(actions)));
            } else {
                setContextMenu(null);
            }
        });
    }

    public class Initializer {

        Function<T, Collection<FxAction>> tableActions = e -> Collections.emptyList();
        Supplier<TableRow<T>> rowSupplier = TableRow::new;

        public Initializer setRowSupplier(Supplier<TableRow<T>> rowSupplier) {
            this.rowSupplier = rowSupplier;
            return this;
        }

        public Initializer setTableActions(Function<T, Collection<FxAction>> tableActions) {
            this.tableActions = tableActions;
            return this;
        }

        protected Map<SpecialAction, Collection<FxAction>> group(Collection<FxAction> actions) {
            return actions.stream()
                    .filter(v -> v.specialAction != null)
                    .collect(Collectors.groupingBy(v -> v.specialAction, Collectors.toCollection(ArrayList::new)));
        }
    }
}
