/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.ide.configurations;

import org.marid.IdeDependants;
import org.marid.dependant.iconviewer.IconViewerConfiguration;
import org.marid.dependant.log.LogConfiguration;
import org.marid.dependant.monitor.MonitorConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ToolsConfiguration {

    @IdeAction
    public FxAction iconViewerAction(IdeDependants dependants) {
        return new FxAction("icons", "Tools")
                .setIcon("M_OPEN_IN_BROWSER")
                .bindText(ls("Icon viewer"))
                .setEventHandler(event -> dependants.start(IconViewerConfiguration.class, context -> {
                    context.setId("iconViewer");
                    context.setDisplayName("Icon Viewer");
                }));
    }

    @IdeAction
    public FxAction monitorAction(IdeDependants dependants) {
        return new FxAction("monitor", "Tools")
                .setIcon("M_GRAPHIC_EQ")
                .bindText(ls("System monitor"))
                .setEventHandler(event -> dependants.start(MonitorConfiguration.class, context -> {
                    context.setId("monitor");
                    context.setDisplayName("Monitor");
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

    @IdeAction
    public FxAction showLogsAction(IdeDependants dependants) {
        return new FxAction("log", "log", "Tools")
                .setIcon("M_VIEW_LIST")
                .bindText(ls("Show logs"))
                .setEventHandler(event -> dependants.start(LogConfiguration.class, context -> {
                    context.setId("logViewer");
                    context.setDisplayName("Log Viewer");
                }));
    }
}
