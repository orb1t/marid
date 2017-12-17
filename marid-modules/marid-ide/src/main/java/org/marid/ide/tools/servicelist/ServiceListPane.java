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

package org.marid.ide.tools.servicelist;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectClasses;
import org.marid.ide.project.ProjectFileType;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.utils.ListUtils;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

public class ServiceListPane extends BorderPane {

  private final ProjectProfile profile;
  private final String className;
  private final ListView<String> actual = new ListView<>();

  public ServiceListPane(ProjectClasses classes, ProjectProfile profile, String className) {
    this.profile = profile;
    this.className = className;

    final ListView<String> fromClasspath = new ListView<>();

    actual.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    fromClasspath.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    try {
      final Path serviceFile = profile.get(ProjectFileType.SERVICES, className);
      if (Files.isRegularFile(serviceFile)) {
        Files.readAllLines(serviceFile, StandardCharsets.UTF_8).stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .forEach(actual.getItems()::add);
      }
    } catch (Exception x) {
      log(WARNING, "Unable to load service list from the project", x);
    }

    try {
      final Class<?> serviceType = Class.forName(className, false, profile.getClassLoader());
      classes.classes(profile, Collections.emptySet()).stream()
          .filter(serviceType::isAssignableFrom)
          .filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
          .map(Class::getName)
          .sorted()
          .forEach(fromClasspath.getItems()::add);
    } catch (Exception x) {
      log(WARNING, "Unable to load service list from the classpath", x);
    }

    final SplitPane splitPane = new SplitPane(actual, fromClasspath);
    setCenter(splitPane);

    final ToolBar toolBar = MaridActions.toolbar(List.of(
        new FxAction("ud")
            .setEventHandler(event -> ListUtils.up(actual.getItems(), actual.getSelectionModel()))
            .bindText("Up")
            .setIcon("D_ARROW_UP")
            .bindDisabled(ListUtils.upDisabled(actual.getItems(), actual.getSelectionModel())),
        new FxAction("ud")
            .setEventHandler(event -> ListUtils.down(actual.getItems(), actual.getSelectionModel()))
            .bindText("Down")
            .setIcon("D_ARROW_DOWN")
            .bindDisabled(ListUtils.downDisabled(actual.getItems(), actual.getSelectionModel())),
        new FxAction("rm")
            .setEventHandler(event -> ListUtils.remove(actual.getItems(), actual.getSelectionModel()))
            .bindText("Remove")
            .setIcon("D_PLAYLIST_REMOVE")
            .bindDisabled(ListUtils.removeDisabled(actual.getSelectionModel())),
        new FxAction("rm")
            .setEventHandler(event -> actual.getItems().clear())
            .bindText("Clear")
            .setIcon("D_ERASER")
            .bindDisabled(ListUtils.clearDisabled(actual.getItems())),
        new FxAction("z")
            .setEventHandler(event -> fromClasspath.getSelectionModel().getSelectedItems().stream()
                .filter(c -> !actual.getItems().contains(c))
                .forEach(actual.getItems()::add))
            .bindText("Move")
            .setIcon("D_ARROW_LEFT")
            .bindDisabled(Bindings.createBooleanBinding(() -> {
              final List<String> elements = new ArrayList<>(fromClasspath.getSelectionModel().getSelectedItems());
              elements.removeAll(actual.getItems());
              return elements.isEmpty();
            }, fromClasspath.getSelectionModel().getSelectedItems(), actual.getSelectionModel().getSelectedItems()))
    ));
    setTop(toolBar);

    setPrefSize(800, 800);
  }

  public void save() {
    try {
      final Path file = profile.get(ProjectFileType.SERVICES, className);
      Files.write(file, actual.getItems(), StandardCharsets.UTF_8);
    } catch (Exception x) {
      log(WARNING, "Unable to save services", x);
    }
  }
}
