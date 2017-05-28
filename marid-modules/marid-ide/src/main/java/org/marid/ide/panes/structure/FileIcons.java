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

package org.marid.ide.panes.structure;

import javafx.scene.Node;
import org.codehaus.plexus.util.FileUtils;
import org.marid.ide.common.IdeShapes;
import org.marid.jfx.icons.FontIcons;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class FileIcons {

    public Node icon(Path path, int size) {
        final String fileName = path.getFileName().toString();
        final String extension = FileUtils.extension(fileName);
        switch (extension) {
            case "java":
                return IdeShapes.javaFile(path.hashCode(), 16);
            case "jar":
                return FontIcons.glyphIcon("M_ARCHIVE", 16);
            case "xml":
                return FontIcons.glyphIcon("M_CODE", 16);
            case "lst":
                return FontIcons.glyphIcon("M_LIST", 16);
            case "properties":
                return FontIcons.glyphIcon("F_PARAGRAPH", 16);
            case "":
                switch (fileName) {
                    case "profiles":
                        return FontIcons.glyphIcon("M_FOLDER_SHARED", 16);
                    case "src":
                        return FontIcons.glyphIcon("O_LOCATION", 16);
                    case "main":
                        return FontIcons.glyphIcon("M_MEMORY", 16);
                    case "test":
                        return FontIcons.glyphIcon("M_BUG_REPORT", 16);
                    case "java":
                        return FontIcons.glyphIcon("F_CUBE", 16);
                    case "resources":
                        return FontIcons.glyphIcon("F_TH_LIST", 16);
                    default:
                        return FontIcons.glyphIcon("M_FOLDER", 16);
                }
            default:
                return null;
        }
    }
}
