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

package org.marid.ide.configurations;

import org.marid.IdeDependants;
import org.marid.dependant.monitor.MonitorConfiguration;
import org.marid.ide.tools.iconviewer.IconViewer;
import org.marid.ide.tools.log.LogStage;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ToolsConfiguration {

    @IdeAction
    public FxAction iconViewerAction(ObjectFactory<IconViewer> iconViewerFactory) {
        return new FxAction("icons", "Tools")
                .setIcon("M_OPEN_IN_BROWSER")
                .bindText(ls("Icon viewer"))
                .setEventHandler(event -> iconViewerFactory.getObject().show());
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
    public FxAction showLogsAction(ObjectFactory<LogStage> logStageFactory) {
        return new FxAction("log", "log", "Tools")
                .setIcon("M_VIEW_LIST")
                .bindText(ls("Show logs"))
                .setEventHandler(event -> logStageFactory.getObject().show());
    }
}
