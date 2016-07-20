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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.apache.maven.model.Model;
import org.marid.dependant.project.config.deps.DependenciesEditor;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectManager;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan(basePackageClasses = {ProjectConfigConfiguration.class})
public class ProjectConfigConfiguration {

    @Bean
    public Model model(ProjectManager projectManager) {
        return projectManager.getProfile().getModel();
    }

    @Bean(name = "Dependencies")
    @Qualifier("projectConf")
    public DependenciesEditor mainDependencyEditor(Model model) {
        return new DependenciesEditor(model.getDependencies());
    }

    @Bean(name = "Configuration dependencies")
    @Qualifier("projectConf")
    public DependenciesEditor confDependencyEditor(Model model) {
        return new DependenciesEditor(model.getProfiles().stream()
                .filter(p -> "conf".equals(p.getId()))
                .findAny()
                .orElse(null)
                .getDependencies());
    }

    @Bean
    public TabPane tabPane(@Qualifier("projectConf") Map<String, Node> nodes) {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        nodes.forEach((name, node) -> {
            final Tab tab = new Tab(L10n.s(name), new MaridScrollPane(node));
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
            tabPane.getTabs().add(tab);
        });
        return tabPane;
    }

    @Bean
    public Dialog<Boolean> dialog(IdePane idePane, TabPane tabPane, ProjectManager projectManager) {
        final Dialog<Boolean> dialog = new Dialog<>();
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
        dialog.setTitle(L10n.s("Project preferences: %s", projectManager.getProfile()));
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(idePane.getScene().getWindow());
        dialog.setResultConverter(type -> true);
        return dialog;
    }

    @Bean
    public ApplicationListener<ContextStartedEvent> onStartListener(Dialog<Boolean> dialog) {
        return event -> dialog.showAndWait();
    }
}
