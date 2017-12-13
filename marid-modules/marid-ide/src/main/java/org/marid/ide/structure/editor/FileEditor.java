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

import org.jetbrains.annotations.PropertyKey;
import org.marid.jfx.action.SpecialAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public interface FileEditor {

  @Nullable
  Runnable getEditAction(@NotNull Path path);

  @NotNull
  String getName();

  @NotNull
  String getIcon();

  @NotNull
  String getGroup();

  @Nullable
  default SpecialAction getSpecialAction() {
    return null;
  }

  default String icon(@PropertyKey(resourceBundle = "fonts.meta") String icon) {
    return icon;
  }
}
