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

package org.marid.dependant.project.config;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.scenes.IdeScene;
import org.marid.l10n.L10nSupport;
import org.marid.pref.PrefSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.jfx.ScrollPanes.scrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectDialog extends Dialog<Model> implements PrefSupport, L10nSupport {

    @Autowired
    public ProjectDialog(IdeScene ideScene, ProjectManager projectManager) {
        final Model model = initModel(projectManager.getProfile());
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
        model.getDependencies().removeIf(d -> "marid-runtime".equals(d.getArtifactId()) && "org.marid".equals(d.getGroupId()));
        return model;
    }

    private TabPane tabPane(Model model) {
        final TabPane tabPane = new TabPane(
                new Tab(s("Common"), scrollPane(new CommonTab(model))),
                new Tab(s("Dependencies"), scrollPane(new DependenciesTab(model))),
                new Tab(s("Repositories"), scrollPane(new RepositoriesTab(model))),
                new Tab(s("Properties"), scrollPane(new PropertiesTab(model)))
        );
        for (final Tab tab : tabPane.getTabs()) {
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }
}
