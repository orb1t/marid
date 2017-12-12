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

package org.marid.ide.configurations;

import org.marid.dependant.monitor.MonitorConfiguration;
import org.marid.ide.IdeDependants;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.tools.iconviewer.IconViewer;
import org.marid.idelib.spring.annotation.IdeAction;
import org.marid.jfx.action.FxAction;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ToolsConfiguration {

  @IdeAction
  public FxAction iconViewerAction(IdePane idePane) {
    return new FxAction("icons", "Tools")
        .setIcon("M_OPEN_IN_BROWSER")
        .bindText(ls("Icon viewer"))
        .setEventHandler(event -> new IconViewer(idePane).show());
  }

  @IdeAction
  public FxAction monitorAction(IdeDependants dependants) {
    return new FxAction("monitor", "Tools")
        .setIcon("M_GRAPHIC_EQ")
        .bindText(ls("System monitor"))
        .setEventHandler(event -> dependants.run(c -> {
          c.register(MonitorConfiguration.class);
          c.setDisplayName("Monitor");
        }));
  }

  @IdeAction
  public FxAction garbageCollectAction() {
    return new FxAction("monitor", "Tools")
        .setIcon("M_CHEVRON_LEFT")
        .bindText(ls("Run garbage collection"))
        .setEventHandler(event -> {
          System.gc();
          System.runFinalization();
        });
  }
}
