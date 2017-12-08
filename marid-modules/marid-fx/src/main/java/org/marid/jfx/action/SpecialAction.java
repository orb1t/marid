/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.action;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCombination;

import javax.annotation.Nonnull;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialAction extends FxAction {

  final String text;
  final KeyCombination accelerator;
  final String icon;
  final String description;

  public SpecialAction(@Nonnull String toolbarGroup,
                       @Nonnull String group,
                       @Nonnull String menu,
                       @Nonnull String text,
                       @Nonnull KeyCombination accelerator,
                       @Nonnull String icon,
                       @Nonnull String description) {
    super(toolbarGroup, group, menu);

    super.text = ls(this.text = text);
    super.accelerator = new SimpleObjectProperty<>(this.accelerator = accelerator);
    super.icon = new SimpleStringProperty(this.icon = icon);
    super.description = ls(this.description = description);
    super.disabled = new SimpleBooleanProperty(true);
  }

  void reset() {
    super.accelerator = new SimpleObjectProperty<>(accelerator);
    super.description = ls(description);
    super.text = ls(text);
    super.icon = new SimpleStringProperty(icon);
    super.disabled = new SimpleBooleanProperty(true);

    super.selected = null;
    super.eventHandler = null;
    super.children = null;

    invalidate();
  }

  void copy(@Nonnull FxAction action) {
    bindText(action.text);
    bindAccelerator(action.accelerator);
    bindIcon(action.icon);
    bindDescription(action.description);
    bindDisabled(action.disabled);
    bindSelected(action.selected);
    bindChildren(action.children);
    bindEventHandler(action.eventHandler);

    invalidate();
  }
}
