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
import org.marid.ide.profile.ProfileLogHandler;
import org.marid.ide.swing.gui.IdeFrameImpl;
import org.marid.ide.widgets.CloseableWidget;
import org.marid.ide.widgets.Widget;
import org.marid.swing.MaridAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;

import java.awt.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author Dmitry Ovchinnikov
 */
@CloseableWidget
@MetaInfo(name = "Profile Management")
public class ProfileManagementWidget extends Widget {

    private final Profile profile;
    private final MaridAction runAction;
    private final MaridAction stopAction;
    private final ProfileLogHandler logHandler;

    @Autowired
    public ProfileManagementWidget(IdeFrameImpl owner, ProfileManager profileManager) {
        super(owner, "Profile Management: %s", profileManager.getCurrentProfile());
        info("Initialized");
        this.profile = profileManager.getCurrentProfile();
        toolBar.add(runAction = new MaridAction("Run", "start", (a, e) -> {
            profile.start();
        }).setEnabledState(!profile.isStarted())).setFocusable(false);
        toolBar.add(stopAction = new MaridAction("Stop", "stop", (a, e) -> {
            profile.stop();
        }).setEnabledState(profile.isStarted())).setFocusable(false);
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
                    runAction.setEnabled(true);
                    stopAction.setEnabled(false);
                });
            }
        });
        this.logHandler = new ProfileLogHandler(profile, new LogHandler());
        profile.addLogHandler(logHandler);
    }

    @Override
    public void dispose() {
        try {
            profile.removeLogHandler(logHandler);
        } finally {
            super.dispose();
        }
    }

    protected class LogHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            System.out.print(new SimpleFormatter().format(record));
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }
}
