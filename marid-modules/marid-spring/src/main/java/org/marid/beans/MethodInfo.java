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
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class MethodInfo extends TypeInfo {

    @Nonnull
    public final TypeInfo[] parameters;

    public MethodInfo(@Nonnull String name,
                      @Nonnull ResolvableType type,
                      @Nullable String title,
                      @Nullable String description,
                      @Nullable String icon,
                      @Nullable Class<?> editor,
                      @Nonnull TypeInfo[] parameters) {
        super(name, type, title, description, icon, editor);
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

    @Override
    public int hashCode() {
        return Arrays.hashCode(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        return Arrays.equals(parameters, ((MethodInfo) obj).parameters);
    }
}
