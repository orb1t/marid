/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.beaned.data;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.marid.ide.beaned.BeanTree;
import org.marid.jfx.icons.FontIcons;

import static javafx.beans.binding.Bindings.format;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataGraphicFactory {

    public static Node getGraphic(BeanTree beanTree, Data data) {
        if (data instanceof BeanData) {
            return graphic(beanTree, (BeanData) data);
        } else if (data instanceof RefData) {
            return graphic(beanTree, (RefData) data);
        }
        return null;
    }

    static Node graphic(BeanTree beanTree, BeanData data) {
        final HBox hBox = new HBox(10);
        if (data.getFactoryMethod() != null) {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(OctIcon.LINK_EXTERNAL, 16);
            final Label label = new Label(null, icon);
            label.textProperty().bind(format("%s.%s", data.factoryBeanProperty(), data.factoryMethodProperty()));
            hBox.getChildren().add(label);
        }
        {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(MaterialIcon.DIRECTIONS_RUN, 16);
            final Label label = new Label(null, icon);
            label.visibleProperty().bind(Bindings.isNotNull(data.initMethodProperty()));
            label.textProperty().bind(data.initMethodProperty());
            hBox.getChildren().add(label);
        }
        {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(MaterialIcon.CLOSE, 16);
            final Label label = new Label(null, icon);
            label.visibleProperty().bind(Bindings.isNotNull(data.destroyMethodProperty()));
            label.textProperty().bind(data.destroyMethodProperty());
            hBox.getChildren().add(label);
        }
        return hBox;
    }

    static Node graphic(BeanTree beanTree, RefData data) {
        final HBox box = new HBox(10);
        {
            final Label label = new Label();
            label.textProperty().bind(data.valueProperty());
            label.visibleProperty().bind(Bindings.isNotNull(data.valueProperty()));
            box.getChildren().add(label);
        }
        {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(OctIcon.LINK_EXTERNAL, 16);
            final Label label = new Label(null, icon);
            label.textProperty().bind(data.refProperty());
            label.visibleProperty().bind(Bindings.isNotNull(data.refProperty()));
            box.getChildren().add(label);
        }
        return box;
    }
}
