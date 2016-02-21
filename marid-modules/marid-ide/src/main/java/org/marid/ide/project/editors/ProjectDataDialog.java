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

package org.marid.ide.project.editors;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.scenes.IdeScene;
import org.marid.l10n.L10nSupport;
import org.marid.pref.PrefSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectDataDialog extends Dialog<Model> implements PrefSupport, L10nSupport {

    @Inject
    public ProjectDataDialog(IdeScene ideScene, ProjectProfile profile) {
        final Model model = initModel(profile);
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefSize(800, 600);
        dialogPane.setContent(tabPane(model));
        dialogPane.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        setTitle("Project preferences");
        initModality(Modality.WINDOW_MODAL);
        initOwner(ideScene.getWindow());
        setResizable(true);
        setResultConverter(type -> type == ButtonType.APPLY ? model : null);
    }

    private Model initModel(ProjectProfile profile) {
        final Model model = profile.getModel().clone();
        if (model.getOrganization() == null) {
            model.setOrganization(new Organization());
        }
        if (model.getDependencies().isEmpty()) {
            DependenciesTab.useDefaultDependencies(model.getDependencies());
        }
        return model;
    }

    private TabPane tabPane(Model model) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Common"), new CommonTab(model)),
                new Tab(s("Dependencies"), scrollPane(new DependenciesTab(model))),
                new Tab(s("Repositories"), scrollPane(new RepositoriesTab(model)))
        );
        for (final Tab tab : tabPane.getTabs()) {
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    private ScrollPane scrollPane(Node node) {
        final ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }
}
