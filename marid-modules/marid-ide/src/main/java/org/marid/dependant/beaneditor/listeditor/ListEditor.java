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

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.cell.TextFieldListCell;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.ide.panes.main.IdeToolbar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.control.CommonListView;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.list.MaridListActions;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.jfx.props.WritableValueImpl;
import org.marid.spring.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.marid.jfx.LocalizedStrings.fls;
import static org.springframework.core.ResolvableType.forClass;
import static org.springframework.core.ResolvableType.forType;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ListEditor extends CommonListView<DElement<?>> {

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
                textProperty().unbind();
                if (item == null || empty) {
                    setContextMenu(null);
                    setGraphic(null);
                } else {
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
                        final ObservableStringValue name = new SimpleStringProperty("element");
                        final ResolvableType elementType = elementType(type);
                        final ValueMenuItems items = new ValueMenuItems(element, elementType, name);
                        factory.initializeBean(items, null);
                        items.addTo(m);
                    }));
                }
            }
        });
    }

    private ResolvableType elementType(ResolvableType type) {
        if (type.isArray()) {
            return type.getComponentType();
        } else {
            final ResolvableType collectionType = forType(type.getType(), forClass(Collection.class));
            return collectionType.getGeneric(0);
        }
    }

    @Autowired
    private void initAddAction(FxAction addAction) {
        addAction.on(this, action -> action.setEventHandler(event -> getItems().add(new DValue("#{null}"))));
    }

    @Autowired
    private void initToolbar(IdeToolbar toolbar) {
        toolbar.on(this, () -> ImmutableMap.of(
                "up", MaridListActions.upAction(this),
                "down", MaridListActions.downAction(this)
        ));
    }
}
