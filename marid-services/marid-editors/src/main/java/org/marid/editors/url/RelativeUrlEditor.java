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

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.marid.dependant.resources.ResourcesTracker;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.tree.TreeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

import static java.util.Comparator.comparingInt;
import static org.marid.jfx.LocalizedStrings.fls;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@Import({ResourcesTracker.class})
public class RelativeUrlEditor {

    @Bean
    public Path resourcesPath(ResourcesTracker tracker) {
        return tracker.getResourcesPath();
    }

    @Bean
    public PathMatcher urlFilter(List<ExtensionFilter> filters, Path resourcesPath) {
        final FileSystem fileSystem = resourcesPath.getFileSystem();
        return filters.stream()
                .flatMap(f -> f.getExtensions().stream())
                .map(pattern -> fileSystem.getPathMatcher("glob:" + pattern))
                .reduce((m1, m2) -> p -> m1.matches(p) || m2.matches(p))
                .orElse(p -> false);
    }

    @Bean
    public Data resources(PathMatcher pathMatcher, ResourcesTracker tracker) {
        return new Data(tracker.resources.filtered(pathMatcher::matches));
    }

    @Bean
    public TreeView<Item> resourceTree(Data data, Path resourcesPath) {
        final TreeView<Item> tree = new TreeView<>(new TreeItem<>(new Item(resourcesPath)));
        tree.setShowRoot(true);
        data.list.forEach(path -> {
            final TreeItem<Item> min = TreeUtils.treeStream(tree)
                    .min(comparingInt(p -> resourcesPath.relativize(p.getValue().path).getNameCount()))
                    .orElseThrow(IllegalStateException::new);
            min.getChildren().add(new TreeItem<>(new Item(path)));
        });
        return tree;
    }

    @Bean
    public BorderPane borderPane(TreeView<Item> tree) {
        final BorderPane pane = new BorderPane();
        pane.setCenter(new MaridScrollPane(tree));
        return pane;
    }

    @Bean(initMethod = "show")
    public Stage stage(ProjectProfile profile, BorderPane borderPane) {
        final Stage stage = new Stage();
        stage.titleProperty().bind(fls("[%s] Relative URL selection", profile.getName()));
        stage.setScene(new Scene(borderPane, 800, 600));
        return stage;
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