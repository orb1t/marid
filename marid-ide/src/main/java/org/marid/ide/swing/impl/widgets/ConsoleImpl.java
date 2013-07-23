/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.swing.impl.widgets;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.marid.Scripting;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

import static javax.swing.JSplitPane.VERTICAL_SPLIT;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConsoleImpl extends AbstractSwingWidget implements ResizableWidget {

    private static final Logger log = Logger.getLogger(ConsoleImpl.class.getName());
    private final RSyntaxDocument document;
    private final RSyntaxTextArea commandLine;
    private final RTextScrollPane commandLineScroller;
    private final JSplitPane splitPane;
    private final JPanel results;
    private final JScrollPane resultsScroller;
    private double ratio;

    public ConsoleImpl() {
        super("Console", "console");
        document = new RSyntaxDocument(Scripting.SCRIPTING.getMime());
        commandLine = new RSyntaxTextArea(document);
        results = new JPanel();
        resultsScroller = new JScrollPane(results);
        commandLineScroller = new RTextScrollPane(commandLine, true);
        splitPane = new JSplitPane(VERTICAL_SPLIT, resultsScroller, commandLineScroller);
        add(splitPane);
        setPreferredSize(new Dimension(500, 500));
        pack();
        splitPane.setDividerLocation((getHeight() * 66) / 100);
    }

    @Override
    public void beginResizing() {
        ratio = resultsScroller.getHeight() / (double) getHeight();
    }

    @Override
    public void endResizing() {
        splitPane.setDividerLocation(ratio);
    }

    @Override
    public void onResize() {
        splitPane.setDividerLocation(ratio);
    }
}
