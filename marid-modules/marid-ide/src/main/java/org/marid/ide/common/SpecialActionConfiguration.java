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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.marid.jfx.action.SpecialAction;
import org.marid.jfx.action.SpecialActions;
import org.marid.idelib.spring.annotation.IdeAction;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
    return new SpecialAction("edit", "ed", "Edit")
        .bindText("Edit")
        .setAccelerator(KeyCombination.valueOf("F4"))
        .setIcon("M_EDIT")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction addAction() {
    return new SpecialAction("mod", "mod", "Edit")
        .bindText("Add")
        .setAccelerator(KeyCombination.valueOf("Ctrl+I"))
        .setIcon("M_ADD_BOX")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction removeAction() {
    return new SpecialAction("mod", "mod", "Edit")
        .bindText("Remove")
        .setAccelerator(KeyCombination.valueOf("F8"))
        .setIcon("D_MINUS_BOX")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction cutAction() {
    return new SpecialAction("cp", "cp", "Edit")
        .bindText("Cut")
        .setAccelerator(KeyCombination.valueOf("Ctrl+X"))
        .setIcon("M_CONTENT_CUT")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction copyAction() {
    return new SpecialAction("cp", "cp", "Edit")
        .bindText("Copy")
        .setAccelerator(KeyCombination.valueOf("Ctrl+C"))
        .setIcon("M_CONTENT_COPY")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction pasteAction() {
    return new SpecialAction("cp", "cp", "Edit")
        .bindText("Paste")
        .setAccelerator(KeyCombination.valueOf("Ctrl+V"))
        .setIcon("M_CONTENT_PASTE")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction clearAllAction() {
    return new SpecialAction("mod", "mod", "Edit")
        .bindText("Clear All")
        .setIcon("M_CLEAR_ALL")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction renameAction() {
    return new SpecialAction("edit", "ed", "Edit")
        .bindText("Rename")
        .setAccelerator(KeyCombination.valueOf("Ctrl+R"))
        .setIcon("D_RENAME_BOX")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction selectAllAction() {
    return new SpecialAction("sel", "sel", "Edit")
        .bindText("Select All")
        .setAccelerator(KeyCombination.valueOf("Ctrl+A"))
        .setIcon("D_SELECT_ALL")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction upAction() {
    return new SpecialAction("ed1", "ed1", "Edit")
        .bindText("Up")
        .setAccelerator(new KeyCodeCombination(KeyCode.UP, KeyCombination.CONTROL_DOWN))
        .setIcon("D_ARROW_UP")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction downAction() {
    return new SpecialAction("ed1", "ed1", "Edit")
        .bindText("Down")
        .setAccelerator(new KeyCodeCombination(KeyCode.DOWN, KeyCombination.CONTROL_DOWN))
        .setIcon("D_ARROW_DOWN")
        .setDisabled(true);
  }

  @IdeAction
  public SpecialAction miscAction() {
    return new SpecialAction("misc", "misc", "Edit")
        .bindText("Item action")
        .setIcon("D_MARTINI")
        .setDisabled(true);
  }
}
