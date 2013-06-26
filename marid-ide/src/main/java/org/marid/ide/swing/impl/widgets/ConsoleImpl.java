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
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.marid.Scripting;

import javax.script.ScriptEngine;
import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;
import java.util.logging.Logger;

import static javax.swing.JSplitPane.VERTICAL_SPLIT;

/**
 * @author Dmitry Ovchinnikov
 */
public class ConsoleImpl extends AbstractSwingWidget implements ResizableWidget {

    private static final long serialVersionUID = -8384777568633611226L;
    private static final Logger log = Logger.getLogger(ConsoleImpl.class.getName());
    private static final TreeMap<String, String> extMimeMap = new TreeMap<>();

    static {
        extMimeMap.put("groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY);
        extMimeMap.put("js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        extMimeMap.put("java", SyntaxConstants.SYNTAX_STYLE_JAVA);
        extMimeMap.put("lsp", SyntaxConstants.SYNTAX_STYLE_LISP);
        extMimeMap.put("clj", SyntaxConstants.SYNTAX_STYLE_CLOJURE);
        extMimeMap.put("scala", SyntaxConstants.SYNTAX_STYLE_SCALA);
        extMimeMap.put("py", SyntaxConstants.SYNTAX_STYLE_PYTHON);
        extMimeMap.put("rb", SyntaxConstants.SYNTAX_STYLE_RUBY);
    }

    private final RSyntaxDocument document;
    private final RSyntaxTextArea commandLine;
    private final RTextScrollPane commandLineScroller;
    private final JSplitPane splitPane;
    private final JPanel results;
    private final JScrollPane resultsScroller;
    private final ScriptEngine engine;
    private double ratio;

    public ConsoleImpl() {
        super("Console", "console");
        engine = Scripting.ENGINE;
        document = new RSyntaxDocument(getMime());
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

    private String getMime() {
        for (String ext : engine.getFactory().getExtensions()) {
            if (extMimeMap.containsKey(ext)) {
                return extMimeMap.get(ext);
            }
        }
        return SyntaxConstants.SYNTAX_STYLE_NONE;
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
