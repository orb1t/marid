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

package org.marid.ide.beaneditor.ui;

import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;

import java.nio.file.Path;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueFactory implements Callback<TreeTableColumn.CellDataFeatures<Object, Node>, ObservableValue<Node>> {

    @Override
    public ObservableValue<Node> call(TreeTableColumn.CellDataFeatures<Object, Node> param) {
        final TreeItem<Object> treeItem = param.getValue();
        final Object data = treeItem.getValue();
        if (data instanceof BeanData) {
            final BeanData d = (BeanData) data;
            return Bindings.createObjectBinding(() -> {
                final HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().add(new Label(d.type.get()));
                if (d.factoryBean.isNotEmpty().get()) {
                    box.getChildren().add(new Separator(Orientation.VERTICAL));
                    final String text = d.factoryBean.get() + "." + d.factoryMethod.get();
                    box.getChildren().add(new Label(text, glyphIcon(OctIcon.LINK_EXTERNAL, 16)));
                }
                if (d.initMethod.isNotEmpty().get()) {
                    box.getChildren().add(new Separator(Orientation.VERTICAL));
                    box.getChildren().add(new Label(d.initMethod.get(), glyphIcon(OctIcon.TRIANGLE_RIGHT)));
                }
                if (d.destroyMethod.isNotEmpty().get()) {
                    box.getChildren().add(new Separator(Orientation.VERTICAL));
                    box.getChildren().add(new Label(d.destroyMethod.get(), glyphIcon(OctIcon.STOP)));
                }
                return box;
            }, d.factoryBean, d.factoryMethod, d.initMethod, d.destroyMethod, d.lazyInit);
        } else if (data instanceof ConstructorArg) {
            final ConstructorArg d = (ConstructorArg) data;
            return Bindings.createObjectBinding(() -> box(d.type, d.ref, d.value), d.ref, d.value);
        } else if (data instanceof Property) {
            final Property d = (Property) data;
            return Bindings.createObjectBinding(() -> box(d.type, d.ref, d.value), d.ref, d.value);
        } else if (data instanceof Path) {
            final Path d = (Path) data;
            return new SimpleObjectProperty<>(new Label(d.toUri().toString()));
        } else {
            return new SimpleObjectProperty<>(new Label());
        }
    }

    private static HBox box(StringProperty type, StringProperty ref, StringProperty value) {
        final HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(new Label(type.get()));
        if (ref.isNotEmpty().get()) {
            box.getChildren().add(new Separator(Orientation.VERTICAL));
            box.getChildren().add(new Label(ref.get(), glyphIcon(OctIcon.LINK)));
        }
        if (value.isNotEmpty().get()) {
            box.getChildren().add(new Separator(Orientation.VERTICAL));
            box.getChildren().add(new Label(value.get()));
        }
        return box;
    }
}
