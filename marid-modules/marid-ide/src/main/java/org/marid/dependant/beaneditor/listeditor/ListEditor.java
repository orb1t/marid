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

package org.marid.dependant.beaneditor.listeditor;

import javafx.beans.value.WritableValue;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.jfx.props.WritableValueImpl;
import org.marid.spring.xml.DCollection;
import org.marid.spring.xml.DElement;
import org.marid.spring.xml.DList;
import org.marid.spring.xml.DValue;
import org.marid.spring.xml.DProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.marid.jfx.LocalizedStrings.fls;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ListEditor extends ListView<DElement<?>> {

    @Autowired
    public ListEditor(DCollection<?> collection) {
        super(collection.elements);
    }

    @Autowired
    public void initRowFactory(ResolvableType type, AutowireCapableBeanFactory factory) {
        setCellFactory(param -> new TextFieldListCell<DElement<?>>() {
            @Override
            public void updateItem(DElement<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    textProperty().unbind();
                    setContextMenu(null);
                    setGraphic(null);
                } else {
                    textProperty().unbind();
                    if (item instanceof DValue) {
                        setText(((DValue) item).getValue());
                        setGraphic(FontIcons.glyphIcon(FontIcon.D_COMMENT_TEXT, 16));
                    } else if (item instanceof DList) {
                        textProperty().bind(fls("<%s>", "List"));
                        setGraphic(FontIcons.glyphIcon(FontIcon.M_LIST, 16));
                    } else if (item instanceof DProps) {
                        textProperty().bind(fls("<%s>", "Properties"));
                        setGraphic(FontIcons.glyphIcon(FontIcon.D_VIEW_LIST, 16));
                    }
                    setContextMenu(new MaridContextMenu(m -> {
                        m.getItems().clear();
                        final int index = getIndex();
                        final Consumer<DElement<?>> consumer = e -> getItems().set(index, e);
                        final Supplier<DElement<?>> supplier = () -> getItems().get(index);
                        final WritableValue<DElement<?>> element = new WritableValueImpl<>(consumer, supplier);
                        final ValueMenuItems items = new ValueMenuItems(element, type, Collections.emptyList());
                        factory.initializeBean(items, null);
                        items.addTo(m);
                    }));
                }
            }
        });
    }
}
