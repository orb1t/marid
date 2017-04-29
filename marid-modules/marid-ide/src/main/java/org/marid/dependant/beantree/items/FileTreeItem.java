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

package org.marid.dependant.beantree.items;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanFile;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.D_FILE;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileTreeItem extends AbstractTreeItem<BeanFile> {

    private final ProjectProfile profile;
    private final ObservableValue<String> name;
    private final ObservableValue<String> type;

    public FileTreeItem(ProjectProfile profile, BeanFile file) {
        super(file);
        setGraphic(glyphIcon(D_FILE, 20));
        this.profile = profile;

        name = Bindings.createStringBinding(() -> file.path.get(file.path.size() - 1), file.path);
        type = ls("file");
        valueProperty().bind(Bindings.createStringBinding(file::getFilePath, file.path));
    }

    @Override
    public ObservableValue<String> getName() {
        return name;
    }

    @Override
    public ObservableValue<String> getType() {
        return type;
    }

    @Override
    public Node getValueGraphic() {
        return IdeShapes.fileNode(getElem(), 20);
    }

    public ProjectProfile getProfile() {
        return profile;
    }
}
