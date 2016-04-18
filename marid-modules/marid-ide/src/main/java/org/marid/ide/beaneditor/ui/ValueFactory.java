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
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.RefValue;
import org.marid.jfx.icons.FontIcons;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueFactory implements Callback<CellDataFeatures<Object, Label>, ObservableValue<Label>> {

    @Override
    public ObservableValue<Label> call(CellDataFeatures<Object, Label> param) {
        if (param.getValue().getValue() instanceof RefValue) {
            final RefValue d = (RefValue) param.getValue().getValue();
            return Bindings.createObjectBinding(() -> {
                final Label label = new Label();
                if (d.ref().isNotEmpty().get()) {
                    label.setGraphic(FontIcons.glyphIcon(OctIcon.LINK_EXTERNAL));
                    label.setText(d.ref().get());
                } else {
                    label.setText(d.value().get());
                }
                return label;
            }, d.value(), d.ref());
        } else if (param.getValue().getValue() instanceof Path) {
            final Path d = (Path) param.getValue().getValue();
            return Bindings.createObjectBinding(() -> new Label(d.toString()));
        } else if (param.getValue().getValue() instanceof BeanData) {
            final BeanData d = (BeanData) param.getValue().getValue();
            return Bindings.createObjectBinding(() -> {
                final Label label = new Label("");
                if (d.lazyInit.isEmpty().get() && d.initMethod.isEmpty().get() && d.destroyMethod.isEmpty().get()) {
                    if (d.factoryBean.isNotEmpty().get() || d.factoryMethod.isNotEmpty().get()) {
                        label.setGraphic(FontIcons.glyphIcon(OctIcon.LINK));
                        label.setText(d.factoryBean.get() + "." + d.factoryMethod.get());
                    }
                } else {
                    if (d.factoryBean.isNotEmpty().get() || d.factoryMethod.isNotEmpty().get()) {
                        label.setGraphic(FontIcons.glyphIcon(OctIcon.LINK));
                        label.setText(d.factoryBean.get() + "." + d.factoryMethod.get());
                    }
                    final Map<String, String> map = new LinkedHashMap<>();
                    if (d.lazyInit.isNotEmpty().get()) {
                        map.put("lazy", "true");
                    }
                    if (d.initMethod.isNotEmpty().get()) {
                        map.put("initMethod", d.initMethod.get());
                    }
                    if (d.destroyMethod.isNotEmpty().get()) {
                        map.put("destroyMethod", d.destroyMethod.get());
                    }
                    label.setText(label.getText() + " " + map);
                }
                return label;
            }, d.factoryBean, d.factoryMethod, d.initMethod, d.destroyMethod, d.lazyInit);
        }
        return Bindings.createObjectBinding(Label::new);
    }

}
