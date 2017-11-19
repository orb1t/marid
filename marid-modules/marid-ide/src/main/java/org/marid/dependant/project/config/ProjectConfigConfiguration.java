/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.project.config;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.apache.maven.model.Model;
import org.marid.Ide;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.control.MaridControls;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan
public class ProjectConfigConfiguration {

  private final ProjectProfile profile;

  public ProjectConfigConfiguration(ProjectProfile profile) {
    this.profile = profile;
  }

  @Bean
  public ProjectProfile profile() {
    return profile;
  }

  @Bean
  public Model model(ProjectProfile profile) {
    return profile.getModel();
  }

  @Bean
  @Qualifier("projectConf")
  @Order(4)
  public DependenciesPane mainDependencyEditor(Model model) {
    return new DependenciesPane("Dependencies", model.getDependencies());
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
  public Dialog<Boolean> dialog(TabPane tabPane, ProjectProfile profile) {
    final Dialog<Boolean> dialog = new Dialog<>();
    dialog.getDialogPane().setContent(tabPane);
    dialog.getDialogPane().setPrefSize(800, 600);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);
    dialog.setTitle(s("Project preferences: %s", profile));
    dialog.initOwner(Ide.primaryStage);
    dialog.initStyle(StageStyle.DECORATED);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.setResizable(true);
    dialog.setResultConverter(type -> true);
    Platform.runLater(dialog::showAndWait);
    return dialog;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectConfigConfiguration that = (ProjectConfigConfiguration) o;

    return profile.equals(that.profile);
  }

  @Override
  public int hashCode() {
    return profile.hashCode();
  }
}
