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
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectManager;
import org.marid.l10n.L10nSupport;
import org.marid.pref.PrefSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static org.marid.jfx.ScrollPanes.scrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectDialog extends Dialog<Boolean> implements PrefSupport, L10nSupport {

    @Autowired
    public ProjectDialog(IdePane idePane, ProjectManager projectManager) {
        final Model model = projectManager.getProfile().getModel();
        final DialogPane dialogPane = getDialogPane();
        dialogPane.setPrefSize(800, 600);
        dialogPane.setContent(tabPane(model));
        dialogPane.getButtonTypes().addAll(ButtonType.CLOSE);
        setTitle("Project preferences");
        initModality(Modality.WINDOW_MODAL);
        initOwner(idePane.getScene().getWindow());
        setResizable(true);
        setResultConverter(type -> true);
    }

    @EventListener
    private void onStart(ContextStartedEvent event) {
        showAndWait();
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
