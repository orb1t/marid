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

package org.marid.ide.panes.main;

import javafx.scene.control.ToolBar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.idelib.spring.annotation.IdeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar {

  private final Supplier<List<FxAction>> actionsFactory;

  @Autowired
  public IdeToolbar(@IdeAction Supplier<List<FxAction>> actionsFactory) {
    this.actionsFactory = actionsFactory;
  }

  @EventListener
  private void onIdeStart(ContextStartedEvent event) {
    MaridActions.initToolbar(actionsFactory.get(), this);
  }
}
