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

import org.marid.dependant.beaneditor.BeanConfiguration;
import org.marid.ide.IdeDependants;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;

import static org.marid.ide.project.ProjectFileType.BEANS_XML;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileEditor extends AbstractFileEditor<ProjectProfile> {

  private final ProjectManager projectManager;
  private final IdeDependants dependants;
  private final SpecialAction editAction;

  @Autowired
  public BeanFileEditor(ProjectManager projectManager, IdeDependants dependants, SpecialAction editAction) {
    super(p -> projectManager.getProfile(p).map(e -> e.get(BEANS_XML).equals(p)).orElse(false));
    this.projectManager = projectManager;
    this.dependants = dependants;
    this.editAction = editAction;
  }

  @NotNull
  @Override
  public String getName() {
    return "Bean File Editor";
  }

  @NotNull
  @Override
  public String getIcon() {
    return icon("M_APPS");
  }

  @NotNull
  @Override
  public String getGroup() {
    return "bean";
  }

  @Override
  protected ProjectProfile editorContext(@NotNull Path path) {
    return projectManager.getProfile(path).orElse(null);
  }

  @Override
  protected void edit(@NotNull Path file, @NotNull ProjectProfile context) {
    dependants.start(new BeanConfiguration(context));
  }

  @Nullable
  @Override
  public SpecialAction getSpecialAction() {
    return editAction;
  }
}
