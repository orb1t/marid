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

package org.marid.ide.structure.editor;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.marid.ide.Ide;
import org.marid.ide.project.ProjectClasses;
import org.marid.ide.project.ProjectFileType;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tools.servicelist.ServiceListPane;
import org.marid.ide.tools.servicelist.ServiceListProvider;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.marid.jfx.LocalizedStrings.ls;

@Component
public class ServiceListEditor extends AbstractFileEditor<Path> {

  private final SpecialAction miscAction;
  private final ServiceListProvider[] serviceListProviders;
  private final ProjectClasses projectClasses;
  private final ProjectManager projectManager;

  @Autowired
  public ServiceListEditor(SpecialAction miscAction,
                           ServiceListProvider[] serviceListProviders,
                           ProjectClasses projectClasses,
                           ProjectManager projectManager) {
    super(path -> projectManager.getProfile(path)
        .map(p -> path.equals(p.get(ProjectFileType.META_INF, "services")))
        .orElse(false));
    this.miscAction = miscAction;
    this.serviceListProviders = serviceListProviders;
    this.projectClasses = projectClasses;
    this.projectManager = projectManager;
  }

  @Nullable
  @Override
  protected Path editorContext(@NotNull Path path) {
    return path;
  }

  @Override
  protected void edit(@NotNull Path path, @NotNull Path context) {
  }

  @NotNull
  @Override
  public String getName() {
    return "Services";
  }

  @NotNull
  @Override
  public String getIcon() {
    return "D_ACCOUNT_EDIT";
  }

  @NotNull
  @Override
  public SpecialAction getSpecialAction() {
    return miscAction;
  }

  @Override
  protected ObservableValue<ObservableList<FxAction>> children(@NotNull Path context) {
    final ProjectProfile profile = projectManager.getProfile(context).orElseThrow(IllegalStateException::new);
    final ObservableList<FxAction> actions = Stream.of(serviceListProviders)
        .map(p -> new FxAction("p", "p", "Providers")
            .setText(p.getServiceClassName())
            .setEventHandler(event -> {
              final ServiceListPane pane = new ServiceListPane(projectClasses, profile, p.getServiceClassName());
              final Dialog<Runnable> dialog = new Dialog<>();
              dialog.getDialogPane().setContent(pane);
              dialog.titleProperty().bind(ls("Service Providers"));
              dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
              dialog.setResultConverter(type -> {
                switch (type.getButtonData()) {
                  case APPLY: return pane::save;
                  default: return null;
                }
              });
              dialog.setResizable(true);
              dialog.initOwner(Ide.primaryStage);
              dialog.initStyle(StageStyle.DECORATED);
              dialog.initModality(Modality.WINDOW_MODAL);
              dialog.showAndWait().ifPresent(Runnable::run);
            })
        )
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
    return new SimpleObjectProperty<>(actions);
  }
}
