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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.apache.maven.model.Dependency;
import org.marid.Ide;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Consumer;

import static javafx.beans.binding.Bindings.isEmpty;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.util.Dependencies.eq;

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
		dependencyTable = new DependencyTable(dependencies.filtered(d -> !eq(d, runtimeDependency())));
	}

	@Autowired
	private void initTable() {
		setCenter(dependencyTable);
	}

	@Autowired
	private void initToolbar() {
		final ObservableList<Dependency> selected = dependencyTable.getSelectionModel().getSelectedItems();
		setBottom(new ToolbarBuilder()
				.add("Add item", "M_ADD", event -> addDependency(new Dependency(), d -> {}))
				.add("Remove item", "M_REMOVE", event -> dependencies.removeAll(selected), isEmpty(selected))
				.addSeparator()
				.add("Clear all items", "M_CLEAR_ALL", event -> clear(), isEmpty(dependencyTable.getItems()))
				.addSeparator()
				.add("Find an artifact", "M_FIND_IN_PAGE", event -> {
					// TODO: add a dialog
				})
				.addSeparator()
				.add("Add standard artifact", "M_ADD_CIRCLE", this::onStandard)
				.build(t -> setMargin(t, new Insets(10, 0, 0, 0))));
	}

	private void clear() {
		final Dependency dependency = runtimeDependency();
		dependencies.removeIf(d -> !eq(d, dependency));
	}

	private void onStandard(ActionEvent event) {
		final String[] artifacts = {"marid-db", "marid-proto"};
		final ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(artifacts[0], artifacts);
		choiceDialog.initModality(Modality.APPLICATION_MODAL);
		choiceDialog.initOwner(Ide.primaryStage);
		choiceDialog.setTitle(s("Standard artifact chooser"));
		choiceDialog.setHeaderText(m("Select a standard artifact") + ": ");
		choiceDialog.showAndWait().ifPresent(artifactId -> addDependency(new Dependency(), d -> {
			d.setGroupId("org.marid");
			d.setArtifactId(artifactId);
			d.setVersion("${marid.version}");
		}));
	}

	private void addDependency(Dependency dependency, Consumer<Dependency> dependencyConsumer) {
		dependencyConsumer.accept(dependency);
		if (dependencies.stream().noneMatch(d -> eq(d, dependency))) {
			dependencies.add(dependency);
		}
	}

	private Dependency runtimeDependency() {
		final Dependency dependency = new Dependency();
		dependency.setGroupId("org.marid");
		dependency.setArtifactId("marid-runtime");
		dependency.setVersion("${marid.version}");
		return dependency;
	}
}
