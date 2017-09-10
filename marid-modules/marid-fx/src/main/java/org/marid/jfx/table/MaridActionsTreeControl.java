package org.marid.jfx.table;

import javafx.beans.binding.Bindings;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialActionType;
import org.marid.jfx.action.SpecialActions;

import javax.annotation.Resource;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public interface MaridActionsTreeControl<T> extends MaridActionsControl<TreeItem<T>> {

    TreeItem<T> getRoot();

    @Resource
    default void setSelectAndRemoveActions(SpecialActions specialActions) {
        if (getClass().isAnnotationPresent(DisableSelectAndRemoveActions.class)) {
            return;
        }

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.REMOVE))
                .bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()))
                .setEventHandler(event -> {
                    final Map<TreeItem<T>, List<TreeItem<T>>> map = getSelectionModel().getSelectedItems().stream()
                            .collect(Collectors.groupingBy(TreeItem::getParent, IdentityHashMap::new, toList()));
                    map.forEach((k, v) -> k.getChildren().removeAll(v));
                })
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.SELECT_ALL))
                .setDisabled(getSelectionModel().getSelectionMode().equals(SelectionMode.SINGLE))
                .setEventHandler(event -> getSelectionModel().selectAll())
        );

        actions().add(e -> new FxAction(specialActions.get(SpecialActionType.CLEAR_ALL))
                .setEventHandler(event -> {
                    if (getSelectionModel().getSelectedItems().isEmpty()) {
                        getRoot().getChildren().clear();
                    } else {
                        getSelectionModel().getSelectedItems().forEach(i -> i.getChildren().clear());
                    }
                })
        );
    }
}
