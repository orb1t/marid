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

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.xml.BeanArg;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.util.MethodUtils;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreeItem extends AbstractTreeItem<BeanData> {

    private final ListSynchronizer<BeanArg, ArgumentTreeItem> argsSynchronizer;
    private final ListSynchronizer<BeanProp, PropertyTreeItem> propsSynchronizer;

    public BeanTreeItem(BeanData elem) {
        super(elem);
        valueProperty().bind(Bindings.createObjectBinding(() -> elem, elem.observables()));
        graphicProperty().bind(Bindings.createObjectBinding(() -> IdeShapes.beanNode(elem, 20), elem.observables()));

        argsSynchronizer = new ListSynchronizer<>(elem.beanArgs, getChildren(), ArgumentTreeItem::new);
        propsSynchronizer = new ListSynchronizer<>(elem.properties, getChildren(), PropertyTreeItem::new);
        setExpanded(true);
    }

    @Override
    public ObservableValue<String> getName() {
        return elem.name;
    }

    @Override
    public ObservableValue<String> getType() {
        final ProjectProfile profile = getProfile();
        return Bindings.createStringBinding(() -> MethodUtils.readableType(profile.getType(elem)), elem.observables());
    }

    @Override
    public ObservableValue<String> valueText() {
        return Bindings.createObjectBinding(() -> {
            if (elem.isFactoryBean()) {
                if (elem.getFactoryBean() != null) {
                    return String.format("%s.%s", elem.getFactoryBean(), elem.getFactoryMethod());
                } else {
                    return String.format("%s.%s", elem.getType(), elem.getFactoryMethod());
                }
            }
            return null;
        }, elem.observables());
    }

    @Override
    public ObservableValue<Node> valueGraphic() {
        return Bindings.createObjectBinding(() -> {
            final HBox box = new HBox(10);
            if (elem.isFactoryBean()) {
                final GlyphIcon<?> icon = FontIcons.glyphIcon(FontIcon.D_LINK, 20);
                box.getChildren().add(icon);
            }
            return box;
        }, elem.observables());
    }
}
