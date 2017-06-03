/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.structure.syneditor;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.marid.spring.annotation.PrototypeComponent;

import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class SynEditor extends BorderPane {

    private final RSyntaxTextArea textArea = new RSyntaxTextArea();
    private final SwingNode swingNode = new SwingNode();

    private Path path;

    public SynEditor() {
        swingNode.setContent(new JScrollPane(textArea, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        setCenter(swingNode);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void load() {
        if (path != null) {
            try {
                final String extension = FilenameUtils.getExtension(path.getFileName().toString());
                if (extension == null) {
                    return;
                }
                switch (extension) {
                    case "java":
                        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                        break;
                    case "js":
                    case "javascript":
                        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                        break;
                    case "properties":
                        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
                        break;
                }

                final String code = new String(Files.readAllBytes(path), UTF_8);
                textArea.setText(code);
            } catch (Exception x) {
                log(WARNING, "Unable to load file", x);
            }
        }
    }
}
