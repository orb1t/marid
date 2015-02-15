/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.widgets.cli;

import org.marid.dyn.MetaInfo;
import org.marid.ide.widgets.Widget;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.actions.InternalFrameAction;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
@MetaInfo(name = "Command Line")
public class CommandLineWidget extends Widget {

    private final JSplitPane splitPane;
    private final CommandLine cmdLine;

    @Autowired
    public CommandLineWidget(CommandLine cmdLine) {
        super("Command Line");
        this.cmdLine = new CommandLine();
        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(cmdLine, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER),
                new JScrollPane(cmdLine.getConsoleArea(), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        splitPane.setOneTouchExpandable(true);
        add(splitPane);
        setPreferredSize(new Dimension(600, 400));
        addInternalFrameListener(new InternalFrameAction(e -> {
            switch (e.getID()) {
                case InternalFrameEvent.INTERNAL_FRAME_OPENED:
                    final double divider = getPref("divider", 0.7);
                    splitPane.setDividerLocation(divider);
                    splitPane.setResizeWeight(divider);
                    break;
            }
        }));
        pack();
    }
}
