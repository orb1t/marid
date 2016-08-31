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

package org.marid.dependant.beaneditor.common;

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.Property;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.beans.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.beans.propeditor.PropEditorConfiguration;
import org.marid.dependant.beaneditor.beans.valueeditor.ValueEditorConfiguration;
import org.marid.spring.xml.MaridDataFactory;
import org.marid.spring.xml.data.array.DArray;
import org.marid.spring.xml.data.collection.DElement;
import org.marid.spring.xml.data.collection.DValue;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.marid.jfx.icons.FontIcon.M_CLEAR;
import static org.marid.jfx.icons.FontIcon.M_MODE_EDIT;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueMenuItems {

    private final IdeDependants dependants;
    private final Supplier<DElement<?>> elementSupplier;
    private final Consumer<DElement<?>> elementConsumer;
    private final Type type;

    public ValueMenuItems(IdeDependants dependants,
                          Supplier<DElement<?>> elementSupplier,
                          Consumer<DElement<?>> elementConsumer,
                          Type type) {
        this.dependants = dependants;
        this.elementSupplier = elementSupplier;
        this.elementConsumer = elementConsumer;
        this.type = type;
    }

    public ValueMenuItems(IdeDependants dependants, Property<DElement<?>> property, Type type) {
        this(dependants, property::getValue, property::setValue, type);
    }

    private Map<String, Object> args(String name) {
        final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();
        mapBuilder.put(name, elementSupplier.get());
        mapBuilder.put("type", type);
        return mapBuilder.build();
    }

    public List<MenuItem> menuItems() {
        final List<MenuItem> items = new ArrayList<>();
        if (elementSupplier.get() != null) {
            final MenuItem clearItem = new MenuItem(s("Clear value"), glyphIcon(M_CLEAR, 16));
            clearItem.setOnAction(ev -> elementConsumer.accept(null));
            items.add(clearItem);
            items.add(new SeparatorMenuItem());
        }
        {
            final MenuItem mi = new MenuItem(s("Edit value..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                if (!(elementSupplier.get() instanceof DValue)) {
                    elementConsumer.accept(MaridDataFactory.create(DValue.class));
                }
                dependants.start(ValueEditorConfiguration.class, args("value"));
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
        if (type != null) {
            if (TypeUtils.isAssignable(type, Properties.class)) {
                final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(e -> {
                    if (!(elementSupplier.get() instanceof DProps)) {
                        elementConsumer.accept(MaridDataFactory.create(DProps.class));
                    }
                    dependants.start(PropEditorConfiguration.class, args("props"));
                });
                items.add(mi);
                items.add(new SeparatorMenuItem());
            } else if (TypeUtils.isAssignable(type, List.class)) {
                final MenuItem mi = new MenuItem(s("Edit list..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(event -> {
                    if (!(elementSupplier.get() instanceof DList)) {
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
                        elementConsumer.accept(list);
                    }
                    dependants.start(ListEditorConfiguration.class, args("list"));
                });
                items.add(mi);
                items.add(new SeparatorMenuItem());
            } else if (TypeUtils.isArrayType(type)) {
                final MenuItem mi = new MenuItem(s("Edit array..."), glyphIcon(M_MODE_EDIT, 16));
                mi.setOnAction(event -> {
                    if (!(elementSupplier.get() instanceof DArray)) {
                        final DArray list = MaridDataFactory.create(DArray.class);
                        final Type componentType = TypeUtils.getArrayComponentType(type);
                        final Class<?> rawType = TypeUtils.getRawType(componentType, null);
                        if (rawType != null) {
                            list.valueType.setValue(rawType.getName());
                        }
                        elementConsumer.accept(list);
                    }
                    dependants.start(ListEditorConfiguration.class, args("list"));
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

    public ContextMenu contextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(menuItems());
        return contextMenu;
    }
}
