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

package org.marid.editors.url;

import javafx.beans.value.WritableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import org.marid.dependant.resources.ResourcesTracker;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.tree.TreeUtils;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import static java.util.Comparator.comparingInt;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class RelativeUrlEditor {

    @Bean
    public Path resourcesPath(ResourcesTracker tracker) {
        return tracker.getResourcesPath();
    }

    @Bean
    public PathMatcher urlFilter(List<ExtensionFilter> filters, Path resourcesPath) {
        return filters.stream()
                .flatMap(f -> f.getExtensions().stream())
                .map(pattern -> resourcesPath.getFileSystem().getPathMatcher("glob:" + pattern))
                .reduce((f1, f2) -> p -> f1.matches(p) || f2.matches(p))
                .orElse(p -> true);
    }

    @Bean
    public Data resources(PathMatcher pathMatcher, ResourcesTracker tracker) {
        return new Data(tracker.resources.filtered(p -> pathMatcher.matches(p.getFileName())));
    }

    @Bean
    public TreeView<Item> resourceTree(Data data, Path resourcesPath) {
        final TreeView<Item> tree = new TreeView<>(new TreeItem<>(new Item(resourcesPath)));
        tree.setShowRoot(true);
        data.list.forEach(path -> Utils.add(tree, path));
        return tree;
    }

    @Bean
    public ListChangeListener<Path> resourcesChangeListener(TreeView<Item> tree, Data data) {
        final ListChangeListener<Path> listener = c -> {
            tree.getRoot().getChildren().clear();
            data.list.forEach(path -> Utils.add(tree, path));
        };
        data.list.addListener(listener);
        return listener;
    }

    @Bean
    public AutoCloseable resourceChangeListenerUnsubscriber(Data data, ListChangeListener<Path> listener) {
        return () -> data.list.removeListener(listener);
    }

    @Bean(initMethod = "show")
    public Dialog<Boolean> stage(TreeView<Item> tree, WritableValue<DElement<?>> value) {
        return new MaridDialog<Boolean>(Modality.NONE)
                .title("URL editor")
                .content(new MaridScrollPane(tree))
                .resizable(true)
                .on(type -> {
                    final TreeItem<Item> selectedItem = tree.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        return;
                    }
                    switch (type.getButtonData()) {
                        case APPLY:
                            final URI uri = selectedItem.getValue().path.toUri();
                            final URI rootUri = tree.getRoot().getValue().path.toUri();
                            value.setValue(new DValue(rootUri.relativize(uri).toString()));
                            break;
                    }
                })
                .buttonTypes(ButtonType.CANCEL, ButtonType.APPLY);
    }
}

class Item {

    final Path path;

    Item(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path.getFileName().toString();
    }
}

class Data {

    final ObservableList<Path> list;

    Data(ObservableList<Path> list) {
        this.list = list;
    }
}

class Utils {

    private static int relativeDistance(TreeItem<Item> item, Path path) {
        if (path.startsWith(item.getValue().path)) {
            return item.getValue().path.relativize(path).getNameCount();
        } else {
            return Integer.MAX_VALUE;
        }
    }

    static void add(TreeView<Item> tree, Path path) {
        TreeUtils.treeStream(tree)
                .min(comparingInt(p -> relativeDistance(p, path)))
                .ifPresent(e -> {
                    for (final Path p : e.getValue().path.relativize(path)) {
                        final TreeItem<Item> newItem = new TreeItem<>(new Item(p));
                        e.getChildren().add(newItem);
                        e.setExpanded(true);
                        e = newItem;
                    }
                });
    }
}