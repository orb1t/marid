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

import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;
import org.marid.ide.Ide;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AddDirectoryEditor extends AbstractFileEditor<Path> {

  private final SpecialAction addAction;

  @Autowired
  public AddDirectoryEditor(SpecialAction addAction) {
    super(Files::isDirectory);
    this.addAction = addAction;
  }

  @Override
  protected void edit(@NotNull Path file, @NotNull Path context) {
    final TextInputDialog dialog = new TextInputDialog();
    dialog.setContentText(s("Directory name") + ":");
    dialog.setTitle(s("Add a directory"));
    dialog.initStyle(StageStyle.UTILITY);
    dialog.initOwner(Ide.primaryStage);
    dialog.initModality(Modality.WINDOW_MODAL);
    dialog.showAndWait().ifPresent(dirName -> {
      final Path path = file.resolve(dirName);
      try {
        Files.createDirectory(path);
      } catch (Exception x) {
        n(WARNING, "Unable to create a directory {0}", x, path);
      }
    });
  }

  @NotNull
  @Override
  public String getName() {
    return "Add a directory";
  }

  @NotNull
  @Override
  public String getIcon() {
    return "M_ADD_CIRCLE";
  }

  @Override
  protected Path editorContext(@NotNull Path path) {
    return path;
  }

  @NotNull
  @Override
  public SpecialAction getSpecialAction() {
    return addAction;
  }
}
