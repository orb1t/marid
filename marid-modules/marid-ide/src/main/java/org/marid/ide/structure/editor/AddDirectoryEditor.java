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

package org.marid.ide.structure.editor;

import javafx.scene.control.TextInputDialog;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
  protected void edit(@Nonnull Path file, @Nonnull Path context) {
    final TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle(s("Add a directory"));
    dialog.setContentText(s("Directory name") + ":");
    final Optional<String> optionalDirectoryName = dialog.showAndWait();
    if (optionalDirectoryName.isPresent()) {
      final String dirName = optionalDirectoryName.get();
      final Path path = file.resolve(dirName);
      try {
        Files.createDirectory(path);
      } catch (Exception x) {
        n(WARNING, "Unable to create a directory {0}", x, path);
      }
    }
  }

  @Nonnull
  @Override
  public String getName() {
    return "Add a directory";
  }

  @Nonnull
  @Override
  public String getIcon() {
    return icon("M_ADD_CIRCLE");
  }

  @Nonnull
  @Override
  public String getGroup() {
    return "file";
  }

  @Override
  protected Path editorContext(@Nonnull Path path) {
    return path;
  }

  @Nullable
  @Override
  public SpecialAction getSpecialAction() {
    return addAction;
  }
}
