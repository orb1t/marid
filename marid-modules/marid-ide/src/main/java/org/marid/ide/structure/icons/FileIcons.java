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
            case "java": return IdeShapes.javaFile(path.hashCode(), size);
            case "jar": return FontIcons.glyphIcon("M_ARCHIVE", size);
            case "xml": return FontIcons.glyphIcon("M_CODE", size);
            case "lst": return FontIcons.glyphIcon("M_LIST", size);
            case "properties": return FontIcons.glyphIcon("D_CODE_ARRAY", size);
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
            default: return null;
        }
    }
}
