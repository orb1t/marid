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

package org.marid.ide.common;

import javafx.scene.input.KeyCodeCombination;
import org.marid.idelib.spring.annotation.IdeAction;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.action.SpecialActions;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.keyCombination;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class SpecialActionConfiguration {

  @Bean
  public SpecialActions specialActions() {
    return new SpecialActions();
  }

  @IdeAction
  public SpecialAction editAction() {
    return new SpecialAction("edit", "ed", "Edit", "Edit", keyCombination("F4"), "M_EDIT", "Edit");
  }

  @IdeAction
  public SpecialAction addAction() {
    return new SpecialAction("mod", "mod", "Edit", "Add", keyCombination("Ctrl+I"), "M_ADD_BOX", "Add");
  }

  @IdeAction
  public SpecialAction removeAction() {
    return new SpecialAction("mod", "mod", "Edit", "Remove", keyCombination("F8"), "D_MINUS_BOX", "Remove");
  }

  @IdeAction
  public SpecialAction cutAction() {
    return new SpecialAction("cp", "cp", "Edit", "Cut", keyCombination("Ctrl+X"), "M_CONTENT_CUT", "Cut");
  }

  @IdeAction
  public SpecialAction copyAction() {
    return new SpecialAction("cp", "cp", "Edit", "Copy", keyCombination("Ctrl+C"), "M_CONTENT_COPY", "Copy");
  }

  @IdeAction
  public SpecialAction pasteAction() {
    return new SpecialAction("cp", "cp", "Edit", "Paste", keyCombination("Ctrl+V"), "M_CONTENT_PASTE", "Paste");
  }

  @IdeAction
  public SpecialAction clearAllAction() {
    return new SpecialAction("mod", "mod", "Edit", "Clear All", keyCombination("Ctrl+Y"), "M_CLEAR_ALL", "Clear All");
  }

  @IdeAction
  public SpecialAction renameAction() {
    return new SpecialAction("edit", "ed", "Edit", "Rename", keyCombination("Ctrl+R"), "D_RENAME_BOX", "Rename");
  }

  @IdeAction
  public SpecialAction selectAllAction() {
    return new SpecialAction("sel", "sel", "Edit", "Select All", keyCombination("Ctrl+A"), "D_SELECT_ALL", "Select All");
  }

  @IdeAction
  public SpecialAction upAction() {
    return new SpecialAction("ed1", "ed1", "Edit", "Up", new KeyCodeCombination(UP, CONTROL_DOWN), "D_ARROW_UP", "Up");
  }

  @IdeAction
  public SpecialAction downAction() {
    return new SpecialAction("ed1", "ed1", "Edit", "Down", new KeyCodeCombination(DOWN, CONTROL_DOWN), "D_ARROW_DOWN", "Down");
  }

  @IdeAction
  public SpecialAction miscAction() {
    return new SpecialAction("misc", "misc", "Edit", "Miscellaneous", keyCombination("Ctrl+M"), "D_MARTINI", "Miscellaneous");
  }
}
