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

package org.marid.jfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import org.marid.jfx.props.BooleanPropertyHolder;
import org.marid.jfx.props.PropertyHolder;
import org.marid.util.Utils;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class Props {

    public static StringProperty stringProperty(Object bean, String name) {
        try {
            return JavaBeanStringPropertyBuilder.create().bean(bean).name(name).build();
        } catch (NoSuchMethodException x) {
            throw new IllegalStateException(x);
        }
    }

    public static BooleanProperty booleanProperty(Object bean, String name) {
        try {
            return JavaBeanBooleanPropertyBuilder.create().bean(bean).name(name).build();
        } catch (NoSuchMethodException x) {
            throw new IllegalStateException(x);
        }
    }

    public static BooleanProperty booleanProperty(BooleanSupplier supplier, Consumer<Boolean> consumer) {
        try {
            return JavaBeanBooleanPropertyBuilder.create()
                    .bean(new BooleanPropertyHolder(supplier, consumer))
                    .name("property")
                    .build();
        } catch (NoSuchMethodException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> Property<T> property(Supplier<T> supplier, Consumer<T> consumer) {
        try {
            return Utils.cast(JavaBeanObjectPropertyBuilder.create()
                    .bean(new PropertyHolder<>(supplier, consumer))
                    .name("property")
                    .build());
        } catch (NoSuchMethodException x) {
            throw new IllegalStateException(x);
        }
    }
}
