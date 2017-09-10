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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.dnd.DndManager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.scene.input.TransferMode.COPY_OR_MOVE;
import static javafx.scene.input.TransferMode.MOVE;
import static org.marid.jfx.action.SpecialActionType.COPY;
import static org.marid.jfx.action.SpecialActionType.PASTE;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTreeTableView<T> extends TreeTableView<T> implements MaridActionsTreeControl<T>, AutoCloseable {

    private final ObservableList<Function<TreeItem<T>, FxAction>> actions = FXCollections.observableArrayList();
    private final List<Observable> observables = new ArrayList<>();
    private final List<Runnable> onConstruct = new ArrayList<>();
    private final List<Runnable> onDestroy = new ArrayList<>();

    @Resource
    protected DndManager dndManager;

    protected Supplier<TreeTableRow<T>> rowSupplier = TreeTableRow::new;

    public MaridTreeTableView(TreeItem<T> root) {
        super(root);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Resource
    public void setSpecialActions(SpecialActions specialActions) {
        final InvalidationListener invalidationListener = o -> {
            if (isFocused()) {
                specialActions.assign(actions(getSelectionModel().getSelectedItem()));
            }
        };
        onConstruct.add(() -> {
            observables.forEach(o -> o.addListener(invalidationListener));
            setRowFactory(param -> {
                final TreeTableRow<T> row = rowSupplier.get();
                row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                    final Collection<FxAction> fxActions = actions(row.getTreeItem());
                    row.setContextMenu(fxActions.isEmpty() ? null : FxAction.grouped(fxActions));
                });
                initDnd(row, specialActions, dndManager);
                return row;
            });
            addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
                final Collection<FxAction> fxActions = actions(new TreeItem<>());
                setContextMenu(fxActions.isEmpty() ? null : FxAction.grouped(fxActions));
            });
            getSelectionModel().selectedItemProperty().addListener(invalidationListener);
            focusedProperty().addListener((o, oV, nV) -> {
                if (nV) {
                    invalidationListener.invalidated(o);
                } else {
                    specialActions.reset();
                }
            });
        });
        onDestroy.add(() -> observables.forEach(o -> o.removeListener(invalidationListener)));
    }

    private Collection<FxAction> actions(TreeItem<T> element) {
        return actions.stream()
                .map(a -> a.apply(element))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void onConstruct() {
        onConstruct.forEach(Runnable::run);
    }

    @Override
    public void close() {
        onDestroy.forEach(Runnable::run);
    }

    @Override
    public ObservableList<Function<TreeItem<T>, FxAction>> actions() {
        return actions;
    }

    @Override
    public List<Observable> observables() {
        return observables;
    }

    @Override
    public List<Runnable> onConstructListeners() {
        return onConstruct;
    }

    @Override
    public List<Runnable> onDestroyListeners() {
        return onDestroy;
    }

    private void initDnd(TreeTableRow<T> row, SpecialActions specialActions, DndManager dndManager) {
        final SpecialAction copyAction = specialActions.get(COPY);
        final SpecialAction pasteAction = specialActions.get(PASTE);

        row.setOnDragDetected(event -> {
            final TreeItem<T> item = row.getTreeItem();
            final Optional<FxAction> copy = actions.stream()
                    .map(f -> f.apply(item))
                    .filter(e -> e != null && e.specialAction == copyAction && !e.isDisabled())
                    .findFirst();
            if (copy.isPresent()) {
                final Dragboard dragboard = row.startDragAndDrop(COPY_OR_MOVE);
                dndManager.updateDragboard(dragboard);
                copy.get().getEventHandler().handle(new ActionEvent(dragboard, row));
                dragboard.setDragView(row.snapshot(null, null));
                event.consume();
            }
        });
        row.setOnDragDone(event -> {
            dndManager.updateDragboard(event.getDragboard());
            final TreeItem<T> item = row.getTreeItem();
            if (item != null && item.getParent() != null && event.getTransferMode() == MOVE) {
                item.getParent().getChildren().remove(item);
            }
            event.consume();
        });

        final Function<TreeItem<T>, Stream<FxAction>> pasteFunc = item -> actions.stream()
                .map(f -> f.apply(item))
                .filter(e -> e != null && e.specialAction == pasteAction && !e.isDisabled());
        row.setOnDragOver(event -> {
            dndManager.updateDragboard(event.getDragboard());
            final Optional<FxAction> paste = pasteFunc.apply(row.getTreeItem()).findFirst();
            if (paste.isPresent()) {
                event.acceptTransferModes(COPY_OR_MOVE);
            }
            event.consume();
        });
        row.setOnDragDropped(event -> {
            dndManager.updateDragboard(event.getDragboard());
            final Optional<FxAction> paste = pasteFunc.apply(row.getTreeItem()).findFirst();
            if (paste.isPresent()) {
                paste.get().getEventHandler().handle(new ActionEvent(event.getDragboard(), row));
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }
}
