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

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.marid.dependant.project.ProjectParams;
import org.marid.dependant.project.config.deps.DependenciesPane;
import org.marid.ide.maven.MavenRepositoryManager;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.control.MaridControls;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan(basePackageClasses = {ProjectConfigConfiguration.class})
public class ProjectConfigConfiguration extends DependantConfiguration<ProjectParams> {

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public Model model(ProjectProfile profile) {
        return profile.getModel();
    }

    @Bean
    @Qualifier("projectConf")
    @Order(4)
    public DependenciesPane mainDependencyEditor(Model model, MavenRepositoryManager manager) {
        return new DependenciesPane("Dependencies", model.getDependencies(), manager.getMaridArtifacts("marid"));
    }

    @Bean
    @Qualifier("projectConf")
    @Order(5)
    public DependenciesPane confDependencyEditor(Model model, MavenRepositoryManager manager) {
        final List<Dependency> list = model.getProfiles().stream()
                .filter(p -> "conf".equals(p.getId()))
                .findAny()
                .orElseThrow(IllegalStateException::new)
                .getDependencies();
        return new DependenciesPane("Configuration dependencies", list, manager.getMaridArtifacts("maridconf"));
    }

    @Bean
    public TabPane tabPane(@Qualifier("projectConf") List<Node> nodes) {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        for (final Node node : nodes) {
            final Tab tab = new Tab(s(node.getId()), MaridControls.createMaridScrollPane(node));
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
