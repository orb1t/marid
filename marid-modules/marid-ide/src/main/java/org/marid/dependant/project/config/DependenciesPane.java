/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.dependant.project.config;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import org.apache.maven.model.Dependency;
import org.marid.ide.maven.MavenArtifactFinder;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

import static javafx.beans.binding.Bindings.createBooleanBinding;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class DependenciesPane extends BorderPane {

    private final ObservableList<Dependency> dependencies;
    private final DependencyTable dependencyTable;

    public DependenciesPane(String name, List<Dependency> deps) {
        setId(name);
        dependencies = FXCollections.observableList(deps);
        dependencyTable = new DependencyTable(dependencies);
    }

    @PostConstruct
    private void initTable() {
        setCenter(dependencyTable);
    }

    private BooleanBinding removeDisabled() {
        final ObservableList<Dependency> selected = dependencyTable.getSelectionModel().getSelectedItems();
        return createBooleanBinding(() -> selected.stream().anyMatch(
                d -> "org.marid".equals(d.getGroupId()) && "marid-runtime".equals(d.getArtifactId())
        ), selected);
    }

    @Autowired
    private void initToolbar(ObjectFactory<MavenArtifactFinder> artifactFinder) {
        final ObservableList<Dependency> selected = dependencyTable.getSelectionModel().getSelectedItems();
        final BooleanBinding empty = createBooleanBinding(this::empty, dependencies);
        setBottom(new ToolbarBuilder()
                .add("Add item", "M_ADD", event -> dependencies.add(new Dependency()))
                .add("Remove item", "M_REMOVE", event -> dependencies.removeAll(selected), removeDisabled())
                .addSeparator()
                .add("Clear all items", "M_CLEAR_ALL", event -> clear(), empty)
                .addSeparator()
                .add("Find an artifact", "M_FIND_IN_PAGE", event -> {
                    final MavenArtifactFinder finder = artifactFinder.getObject();
                    finder.showAndWait().ifPresent(a -> dependencies.add(a.toDependency()));
                })
                .build(t -> setMargin(t, new Insets(10, 0, 0, 0))));
    }

    private boolean empty() {
        switch (dependencies.size()) {
            case 0:
                return true;
            case 1:
                return "org.marid".equals(dependencies.get(0).getGroupId())
                        && "marid-runtime".equals(dependencies.get(0).getArtifactId());
            default:
                return false;
        }
    }

    private void clear() {
        dependencies.removeIf(d -> !"org.marid".equals(d.getGroupId()) && !"marid-runtime".equals(d.getArtifactId()));
    }
}
