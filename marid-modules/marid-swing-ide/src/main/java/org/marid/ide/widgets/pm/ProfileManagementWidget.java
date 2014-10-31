/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.ide.widgets.pm;

import org.marid.dyn.MetaInfo;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.profile.Profile;
import org.marid.ide.swing.gui.IdeFrameImpl;
import org.marid.ide.widgets.CloseableWidget;
import org.marid.ide.widgets.Widget;
import org.marid.logging.Logging;
import org.marid.logging.SimpleHandler;
import org.marid.swing.MaridAction;
import org.marid.swing.component.ResizablePanel;
import org.marid.swing.log.LogComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Handler;

import static java.util.Collections.emptyList;

/**
 * @author Dmitry Ovchinnikov
 */
@CloseableWidget
@MetaInfo(name = "Profile Management")
public class ProfileManagementWidget extends Widget {

    private final Profile profile;
    private final MaridAction runAction;
    private final MaridAction stopAction;
    private final Handler logHandler;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final LogComponent logComponent;
    private final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();

    @Autowired
    public ProfileManagementWidget(IdeFrameImpl owner, ProfileManager profileManager) {
        super(owner, "Profile Management: %s", profileManager.getCurrentProfile());
        info("Initialized");
        this.profile = profileManager.getCurrentProfile();
        logComponent = new LogComponent(preferences(), emptyList(), r -> true);
        this.logHandler = new SimpleHandler((h, r) -> {
            if (profile.getName().equals(itl.get())) {
                logComponent.publish(r);
            }
        });
        toolBar.add(runAction = new MaridAction("Run", "start", (a, e) -> {
            profile.start(() -> {
                itl.set(profile.getName());
                Logging.rootLogger().addHandler(logHandler);
            });
        }).setEnabledState(!profile.isStarted())).setFocusable(false);
        toolBar.add(stopAction = new MaridAction("Stop", "stop", (a, e) -> {
            profile.stop();
        }).setEnabledState(profile.isStarted())).setFocusable(false);
        panel.add(new ResizablePanel<>(logComponent), BorderLayout.SOUTH);
        add(panel);
        pack();
        this.profile.addApplicationListener(event -> {
            info("Event: {0}", event);
            if (event instanceof ContextStartedEvent) {
                EventQueue.invokeLater(() -> {
                    runAction.setEnabled(false);
                    stopAction.setEnabled(true);
                });
            } else if (event instanceof ContextClosedEvent) {
                EventQueue.invokeLater(() -> {
                    Logging.rootLogger().removeHandler(logHandler);
                    runAction.setEnabled(true);
                    stopAction.setEnabled(false);
                });
            }
        });
    }
}
