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

package org.marid.ide.structure.icons;

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
                return IdeShapes.javaFile(path.hashCode(), size);
            case "jar":
                return FontIcons.glyphIcon("M_ARCHIVE", size);
            case "xml":
                return FontIcons.glyphIcon("M_CODE", size);
            case "lst":
                return FontIcons.glyphIcon("M_LIST", size);
            case "properties":
                return FontIcons.glyphIcon("F_PARAGRAPH", size);
            case "":
                switch (path.getParent().getFileName().toString()) {
                    case "profiles":
                        return IdeShapes.circle(fileName.hashCode(), 16);
                    default:
                        switch (fileName) {
                            case "profiles":
                                return FontIcons.glyphIcon("M_FOLDER_SHARED", size);
                            case "src":
                                return FontIcons.glyphIcon("O_LOCATION", size);
                            case "main":
                                return FontIcons.glyphIcon("M_MEMORY", size);
                            case "test":
                                return FontIcons.glyphIcon("M_BUG_REPORT", size);
                            case "java":
                                return FontIcons.glyphIcon("F_CUBE", size);
                            case "resources":
                                return FontIcons.glyphIcon("F_TH_LIST", size);
                            default:
                                return FontIcons.glyphIcon("M_FOLDER", size);
                        }
                }
            default:
                return null;
        }
    }
}
