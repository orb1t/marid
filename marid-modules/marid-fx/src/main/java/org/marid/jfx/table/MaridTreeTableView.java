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

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.action.SpecialActions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTreeTableView<T> extends TreeTableView<T> {

    protected final ObjectProperty<Supplier<TreeTableRow<T>>> rowSupplier = new SimpleObjectProperty<>(TreeTableRow::new);
    protected final ObservableList<Function<TreeItem<T>, FxAction>> actions = FXCollections.observableArrayList();
    protected final SpecialActions specialActions;
    protected final List<Observable> observables = new ArrayList<>();

    public MaridTreeTableView(TreeItem<T> root, SpecialActions specialActions) {
        super(root);
        this.specialActions = specialActions;
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @PostConstruct
    protected void initialize() {
        observables.forEach(o -> o.addListener(this::onInvalidate));
        setRowFactory(param -> {
            final TreeTableRow<T> row = rowSupplier.get().get();
            row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                final Collection<FxAction> fxActions = actions(row.getTreeItem());
                row.setContextMenu(fxActions.isEmpty() ? null : new ContextMenu(MaridActions.contextMenu(fxActions)));
            });
            return row;
        });
        addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            final Collection<FxAction> fxActions = actions(new TreeItem<>());
            setContextMenu(fxActions.isEmpty() ? null : new ContextMenu(MaridActions.contextMenu(fxActions)));
        });
        getSelectionModel().selectedItemProperty().addListener(this::onInvalidate);
        focusedProperty().addListener((o, oV, nV) -> {
            if (nV) {
                onInvalidate(o);
            } else {
                specialActions.reset();
            }
        });
    }

    private Collection<FxAction> actions(TreeItem<T> element) {
        return actions.stream()
                .map(a -> a.apply(element))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @PreDestroy
    protected void onDestroy() {
        observables.forEach(o -> o.removeListener(this::onInvalidate));
    }

    protected void onInvalidate(Observable observable) {
        specialActions.assign(actions(getSelectionModel().getSelectedItem()));
    }
}
