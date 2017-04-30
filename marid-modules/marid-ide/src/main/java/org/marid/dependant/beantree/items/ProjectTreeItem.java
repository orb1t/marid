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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;

import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProjectTreeItem extends AbstractTreeItem<ProjectProfile> {

    private final ObservableValue<String> name;
    private final ObservableValue<String> type;

    public ProjectTreeItem(ProjectProfile elem) {
        super(elem, elem.getBeanFiles());
        name = new SimpleStringProperty(elem.getName());
        type = LocalizedStrings.ls("profile");

        valueProperty().bind(Bindings.createObjectBinding(() -> elem, elem.getBeanFiles()));
        graphicProperty().bind(Bindings.createObjectBinding(() -> IdeShapes.profileNode(elem, 20)));

        getChildren().addAll(elem.getBeanFiles().stream().map(FileTreeItem::new).collect(Collectors.toList()));
        setExpanded(true);
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
    public ObservableValue<Node> valueGraphic() {
        return Bindings.createObjectBinding(() -> FontIcons.glyphIcon(FontIcon.M_PANORAMA, 20));
    }

    @Override
    public ObservableValue<String> valueText() {
        return Bindings.createStringBinding(() -> null);
    }
}
