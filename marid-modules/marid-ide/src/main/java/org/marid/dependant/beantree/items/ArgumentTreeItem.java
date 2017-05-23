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

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.marid.IdeDependants;
import org.marid.dependant.valuemenu.ValuesConfiguration;
import org.marid.dependant.valuemenu.ValuesParams;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.xml.BeanArg;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.DElement;
import org.marid.util.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;

import javax.annotation.Nonnull;

import static java.lang.Integer.compare;
import static java.util.Optional.ofNullable;

/**
 * @author Dmitry Ovchinnikov
 */
@Order(1)
@Configurable
public class ArgumentTreeItem extends DataTreeItem<BeanArg> {

    public ArgumentTreeItem(BeanArg elem) {
        super(elem);

        setGraphic(FontIcons.glyphIcon("D_ALBUM", 20));
    }

    @Override
    public ObservableValue<String> nameProperty() {
        return elem.name;
    }

    @Override
    public ObservableValue<DElement<?>> elementProperty() {
        return elem.data;
    }

    @Override
    public ObservableValue<String> getName() {
        return elem.name;
    }

    @Override
    public ObservableValue<String> getType() {
        return Bindings.createStringBinding(() -> {
            final ProjectProfile profile = getProfile();
            final BeanData data = find(BeanTreeItem.class).elem;
            return MethodUtils.readableType(profile.getArgType(data, elem.getName()));
        }, elem);
    }

    @Override
    public int compareTo(@Nonnull AbstractTreeItem<?> o) {
        if (o instanceof ArgumentTreeItem) {
            final ArgumentTreeItem i = (ArgumentTreeItem) o;
            final BeanData beanData = find(BeanTreeItem.class).elem;
            final int i1 = beanData.beanArgs.indexOf(elem);
            final int i2 = beanData.beanArgs.indexOf(i.elem);
            return compare(i1, i2);
        } else {
            final Order o1 = getClass().getAnnotation(Order.class);
            final Order o2 = o.getClass().getAnnotation(Order.class);
            return compare(ofNullable(o1).map(Order::value).orElse(0), ofNullable(o2).map(Order::value).orElse(0));
        }
    }

    @Autowired
    private void initValueMenuItems(ProjectProfile profile, IdeDependants dependants) {
        menuConsumers.add(items -> {
            final ValuesParams params = new ValuesParams(profile, elem, items);
            dependants.start(ValuesConfiguration.class, params, f -> {});
        });
    }
}
