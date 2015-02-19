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
import org.marid.swing.actions.ActionKeySupport;
import org.marid.swing.actions.InternalFrameAction;
import org.marid.swing.actions.MaridActions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.*;

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
        setJMenuBar(new JMenuBar());
        this.cmdLine = cmdLine;
        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(cmdLine, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER),
                new ConsoleAreaPanel());
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
    }

    @Override
    protected void fillActions() {
        addAction("/Console/c/clear", "Clear", "clean", (a, e) -> {
            cmdLine.clear();
        }).setKey("control N").enableToolbar();
    }

    private class ConsoleAreaPanel extends JPanel {

        private ConsoleAreaPanel() {
            super(new BorderLayout());
            add(new JScrollPane(cmdLine.getConsoleArea(), VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER));
            add(new ConsoleAreaToolbar(), BorderLayout.WEST);
        }
    }

    private class ConsoleAreaToolbar extends JToolBar implements ActionKeySupport {

        private ConsoleAreaToolbar() {
            super(VERTICAL);
            setFloatable(false);
            addAction("/common/c/autoClean", "Auto-clean output", "purge", (a, e) -> {
                cmdLine.setAutoClean((Boolean) a.getValue(Action.SELECTED_KEY));
            }).setSelected(cmdLine.isAutoClean()).enableToolbar();
            addAction("/common/c/clean", "Clear output", "clean", (a, e) -> {
                cmdLine.getConsoleArea().setText("");
            }).enableToolbar();
            MaridActions.fillToolbar(getActionMap(), this);
        }
    }
}
