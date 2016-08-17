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
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.beans.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.beans.propeditor.PropEditorConfiguration;
import org.marid.spring.xml.MaridDataFactory;
import org.marid.spring.xml.data.collection.DElement;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.marid.jfx.icons.FontIcon.M_CLEAR;
import static org.marid.jfx.icons.FontIcon.M_MODE_EDIT;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueMenuItems {

    private final IdeDependants dependants;
    private final Property<DElement<?>> element;
    private final Type type;

    public ValueMenuItems(IdeDependants dependants, Property<DElement<?>> element, Type type) {
        this.dependants = dependants;
        this.element = element;
        this.type = type;
    }

    public List<MenuItem> menuItems() {
        if (type == null) {
            return Collections.emptyList();
        }
        final List<MenuItem> items = new ArrayList<>();
        if (element.getValue() != null) {
            final MenuItem clearItem = new MenuItem(s("Clear value"), glyphIcon(M_CLEAR, 16));
            clearItem.setOnAction(ev -> element.setValue(null));
            items.add(clearItem);
            items.add(new SeparatorMenuItem());
        }
        {
            final MenuItem mi = new MenuItem(s("Edit value..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
        if (TypeUtils.isAssignable(type, Properties.class)) {
            final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(e -> {
                final DProps props;
                if (element.getValue() instanceof DProps) {
                    props = (DProps) element.getValue();
                } else {
                    props = MaridDataFactory.create(DProps.class);
                    element.setValue(props);
                }
                dependants.start(PropEditorConfiguration.class, ImmutableMap.of("props", props));
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        } else if (TypeUtils.isAssignable(type, List.class)) {
            final MenuItem mi = new MenuItem(s("Edit list..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                final DList list;
                if (element.getValue() instanceof DList) {
                    list = (DList) element.getValue();
                } else {
                    list = MaridDataFactory.create(DList.class);
                    element.setValue(list);
                }
                dependants.start(ListEditorConfiguration.class, ImmutableMap.of("list", list));
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
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
