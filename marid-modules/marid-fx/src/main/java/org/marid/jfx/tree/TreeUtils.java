package org.marid.jfx.tree;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface TreeUtils {

    static <T> Stream<TreeItem<T>> treeStream(TreeView<T> treeView) {
        return treeStream(treeView.getRoot());
    }

    static <T> Stream<TreeItem<T>> treeStream(TreeTableView<T> treeTableView) {
        return treeStream(treeTableView.getRoot());
    }

    static <T> Stream<TreeItem<T>> treeStream(TreeItem<T> treeItem) {
        return concat(of(treeItem), treeItem.getChildren().stream().flatMap(TreeUtils::treeStream));
    }
}
