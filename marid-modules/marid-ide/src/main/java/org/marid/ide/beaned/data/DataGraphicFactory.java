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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.marid.ide.beaned.BeanTree;
import org.marid.jfx.icons.FontIcons;

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
        final BorderPane pane = new BorderPane();
        pane.centerProperty().bind(Bindings.createObjectBinding(() -> {
            final HBox box = new HBox(10);
            if (StringUtils.isNotEmpty(data.getFactoryBean())) {
                final GlyphIcon<?> icon = FontIcons.glyphIcon(OctIcon.LINK_EXTERNAL, 16);
                final Label label = new Label(data.getFactoryBean(), icon);
                box.getChildren().add(label);
            }
            if (StringUtils.isNotEmpty(data.getInitMethod())) {
                final GlyphIcon<?> icon = FontIcons.glyphIcon(MaterialIcon.DIRECTIONS_RUN, 16);
                final Label label = new Label(data.getInitMethod(), icon);
                box.getChildren().add(label);
            }
            if (StringUtils.isNotEmpty(data.getDestroyMethod())) {
                final GlyphIcon<?> icon = FontIcons.glyphIcon(MaterialIcon.CLOSE, 16);
                final Label label = new Label(data.getDestroyMethod(), icon);
                box.getChildren().add(label);
            }
            return box;
        }, data.destroyMethodProperty(), data.initMethodProperty(), data.factoryBeanProperty()));
        return pane;
    }

    static Node graphic(BeanTree beanTree, RefData data) {
        final BorderPane pane = new BorderPane();
        pane.centerProperty().bind(Bindings.createObjectBinding(() -> {
            final Label label = new Label();
            if (StringUtils.isNotEmpty(data.getRef())) {
                final GlyphIcon<?> icon = FontIcons.glyphIcon(OctIcon.LINK_EXTERNAL, 16);
                label.setGraphic(icon);
                label.setText(data.getRef());
            }
            if (StringUtils.isNotEmpty(data.getValue())) {
                label.setText(data.getValue());
            }
            return label;
        }, data.refProperty(), data.valueProperty()));
        return pane;
    }
}
