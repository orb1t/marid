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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.apache.maven.model.Model;
import org.marid.dependant.project.config.deps.DependenciesEditor;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.panes.MaridScrollPane;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.annotation.Order;

import java.util.List;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan(basePackageClasses = {ProjectConfigConfiguration.class})
public class ProjectConfigConfiguration {

    @Bean
    public Model model(ProjectProfile profile) {
        return profile.getModel();
    }

    @Bean
    @Qualifier("projectConf")
    @Order(4)
    public DependenciesEditor mainDependencyEditor(Model model) {
        return new DependenciesEditor("Dependencies", model.getDependencies());
    }

    @Bean
    @Qualifier("projectConf")
    @Order(5)
    public DependenciesEditor confDependencyEditor(Model model) {
        return new DependenciesEditor("Configuration dependencies", model.getProfiles().stream()
                .filter(p -> "conf".equals(p.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new)
                .getDependencies());
    }

    @Bean
    public TabPane tabPane(@Qualifier("projectConf") List<Node> nodes) {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (final Node node : nodes) {
            final Tab tab = new Tab(s(node.getId()), new MaridScrollPane(node));
            ((Region) tab.getContent()).setPadding(new Insets(10, 0, 10, 0));
            tab.getContent().setStyle("-fx-background-color: -fx-background");
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

    @Bean
    public Dialog<Boolean> dialog(IdePane idePane, TabPane tabPane, ProjectProfile profile) {
        final Dialog<Boolean> dialog = new Dialog<>();
        dialog.getDialogPane().setPrefSize(800, 600);
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
        dialog.setTitle(s("Project preferences: %s", profile));
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
