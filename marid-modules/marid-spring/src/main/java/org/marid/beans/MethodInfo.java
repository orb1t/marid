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

package org.marid.beans;

import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.Introspector;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class MethodInfo extends TypeInfo implements Formattable {

    @Nonnull
    public final TypeInfo[] parameters;

    public MethodInfo(@Nonnull String name,
                      @Nonnull ResolvableType type,
                      @Nullable String title,
                      @Nullable String description,
                      @Nullable String icon,
                      @Nonnull List<Class<?>> editors,
                      @Nonnull TypeInfo[] parameters) {
        super(name, type, title, description, icon, editors);
        this.parameters = parameters;
    }

    @Override
    protected void map(BiConsumer<String, Object> consumer) {
        super.map(consumer);
        consumer.accept("parameters", Arrays.asList(parameters));
    }

    public boolean isSetter() {
        return name.startsWith("set") && name.length() > 3 && parameters.length == 1;
    }

    public boolean isGetter() {
        if (ResolvableType.forClass(boolean.class).equals(type)) {
            return name.startsWith("is") && name.length() > 2 && parameters.length == 0;
        } else {
            return name.startsWith("get") && name.length() > 3 && parameters.length == 0;
        }
    }

    public boolean isProperty() {
        return isGetter() || isSetter();
    }

    public String getPropertyName() {
        if (isProperty()) {
            if (isGetter()) {
                if (name.startsWith("get")) {
                    return Introspector.decapitalize(name.substring(3));
                } else {
                    return Introspector.decapitalize(name.substring(2));
                }
            } else {
                return Introspector.decapitalize(name.substring(3));
            }
        } else {
            return null;
        }
    }

    public boolean matches(Executable executable) {
        final Class<?>[] parameters = executable.getParameterTypes();
        if (parameters.length != this.parameters.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; i++) {
            if (!parameters[i].equals(this.parameters[i].type.getRawClass())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format("%s", Stream.of(parameters)
                .map(i -> i.name + ": " + i.type)
                .collect(Collectors.joining(",", name + "(", ")")));
    }
}
