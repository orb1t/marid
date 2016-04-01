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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueCell extends TreeTableCell<Object, String> {

    private final TreeTableColumn<Object, String> column;

    public ValueCell(TreeTableColumn<Object, String> column) {
        this.column = column;
        setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    public void updateItem(String val, boolean empty) {
        super.updateItem(val, empty);
        graphicProperty().unbind();
        if (empty || getTreeTableRow() == null || getTreeTableRow().getTreeItem() == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(val);
            final TreeItem<Object> item = getTreeTableRow().getTreeItem();
            if (item.getValue() instanceof BeanData) {
                final BeanData d = (BeanData) item.getValue();
                graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (d.factoryBean.isNotEmpty().get()) {
                        final String text = d.factoryBean.get() + "." + d.factoryMethod.get();
                        return new Label(text, glyphIcon(OctIcon.LINK_EXTERNAL, 20));
                    } else {
                        return new Label("");
                    }
                }, d.factoryBean));
            } else if (item.getValue() instanceof ConstructorArg) {
                final ConstructorArg d = (ConstructorArg) item.getValue();
                graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (d.ref.isNotEmpty().get()) {
                        return new Label(d.ref.get(), glyphIcon(OctIcon.LINK, 20));
                    } else {
                        return new Label("");
                    }
                }, d.ref));
            } else if (item.getValue() instanceof Property) {
                final Property d = (Property) item.getValue();
                graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (d.ref.isNotEmpty().get()) {
                        return new Label(d.ref.get(), glyphIcon(OctIcon.LINK, 20));
                    } else {
                        return new Label("");
                    }
                }, d.ref));
            }
        }
    }
}
