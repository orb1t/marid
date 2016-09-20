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

package org.marid.dependant.beaneditor;

import javafx.beans.value.WritableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.beans.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.beans.propeditor.PropEditorConfiguration;
import org.marid.dependant.beaneditor.beans.valueeditor.ValueEditorConfiguration;
import org.marid.spring.xml.MaridDataFactory;
import org.marid.spring.xml.data.collection.DArray;
import org.marid.spring.xml.data.collection.DElement;
import org.marid.spring.xml.data.collection.DValue;
import org.marid.spring.xml.data.collection.DList;
import org.marid.spring.xml.data.props.DProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ValueMenuItems {

    private final IdeDependants dependants;

    @Autowired
    public ValueMenuItems(IdeDependants dependants) {
        this.dependants = dependants;
    }

    public List<MenuItem> menuItems(WritableValue<DElement<?>> elementProperty, Type type) {
        final List<MenuItem> items = new ArrayList<>();
        if (elementProperty.getValue() != null) {
            final MenuItem clearItem = new MenuItem(s("Clear value"), glyphIcon(M_CLEAR, 16));
            clearItem.setOnAction(ev -> elementProperty.setValue(null));
            items.add(clearItem);
            items.add(new SeparatorMenuItem());
        }
        {
            final MenuItem mi = new MenuItem(s("Edit value..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                if (!(elementProperty.getValue() instanceof DValue)) {
                    elementProperty.setValue(MaridDataFactory.create(DValue.class));
                }
                dependants.start(ValueEditorConfiguration.class, elementProperty.getValue(), type);
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
        {
            final Menu menu = new Menu(s("Reference"), glyphIcon(M_LINK, 16));
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }
        if (type != null) {
            if (TypeUtils.isAssignable(type, Properties.class)) {
                final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(e -> {
                    if (!(elementProperty.getValue() instanceof DProps)) {
                        elementProperty.setValue(MaridDataFactory.create(DProps.class));
                    }
                    dependants.start(PropEditorConfiguration.class, elementProperty.getValue(), type);
                });
                items.add(mi);
                items.add(new SeparatorMenuItem());
            } else if (TypeUtils.isAssignable(type, List.class)) {
                final MenuItem mi = new MenuItem(s("Edit list..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(event -> {
                    if (!(elementProperty.getValue() instanceof DList)) {
                        final DList list = MaridDataFactory.create(DList.class);
                        final Map<TypeVariable<?>, Type> map = TypeUtils.getTypeArguments(type, List.class);
                        if (map != null) {
                            map.forEach((v, t) -> {
                                final Class<?> rawType = TypeUtils.getRawType(v, t);
                                if (rawType != null) {
                                    list.valueType.setValue(rawType.getName());
                                }
                            });
                        }
                        elementProperty.setValue(list);
                    }
                    dependants.start(ListEditorConfiguration.class, elementProperty.getValue(), type);
                });
                items.add(mi);
                items.add(new SeparatorMenuItem());
            } else if (TypeUtils.isArrayType(type)) {
                final MenuItem mi = new MenuItem(s("Edit array..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(event -> {
                    if (!(elementProperty.getValue() instanceof DArray)) {
                        final DArray list = MaridDataFactory.create(DArray.class);
                        final Type componentType = TypeUtils.getArrayComponentType(type);
                        final Class<?> rawType = TypeUtils.getRawType(componentType, null);
                        if (rawType != null) {
                            list.valueType.setValue(rawType.getName());
                        }
                        elementProperty.setValue(list);
                    }
                    dependants.start(ListEditorConfiguration.class, elementProperty.getValue(), type);
                });
                items.add(mi);
                items.add(new SeparatorMenuItem());
            }
        }
        if (!items.isEmpty()) {
            final MenuItem last = items.get(items.size() - 1);
            if (last instanceof SeparatorMenuItem) {
                items.remove(items.size() - 1);
            }
        }
        return items;
    }
}
