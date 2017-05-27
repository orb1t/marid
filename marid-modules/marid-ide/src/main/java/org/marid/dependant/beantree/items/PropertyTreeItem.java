/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree.items;

import javafx.beans.value.ObservableValue;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

import javax.annotation.Nonnull;

import static java.lang.Integer.compare;
import static java.util.Optional.ofNullable;
import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.util.MethodUtils.readableType;

/**
 * @author Dmitry Ovchinnikov
 */
@Order(2)
@Configurable
public class PropertyTreeItem extends DataTreeItem<BeanProp> {

    public PropertyTreeItem(BeanProp elem) {
        super(elem);

        setGraphic(FontIcons.glyphIcon("D_CLOUD_CIRCLE", 20));
    }

    @Override
    public ObservableValue<String> getName() {
        return elem.name;
    }

    @Override
    public ObservableValue<String> getType() {
        return createStringBinding(() -> readableType(elem.resolvableType.get()), elem, elem.resolvableType);
    }

    @Override
    public int compareTo(@Nonnull AbstractTreeItem<?> o) {
        if (o instanceof PropertyTreeItem) {
            final PropertyTreeItem i = (PropertyTreeItem) o;
            final BeanData beanData = find(BeanTreeItem.class).elem;
            final int i1 = beanData.properties.indexOf(elem);
            final int i2 = beanData.properties.indexOf(i.elem);
            return compare(i1, i2);
        } else {
            final Order o1 = getClass().getAnnotation(Order.class);
            final Order o2 = o.getClass().getAnnotation(Order.class);
            return compare(ofNullable(o1).map(Order::value).orElse(0), ofNullable(o2).map(Order::value).orElse(0));
        }
    }
}
