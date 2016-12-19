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
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class TypeInfo {

    @Nonnull
    public final String name;

    @Nonnull
    public final ResolvableType type;

    @Nullable
    public final String title;

    @Nullable
    public final String description;

    @Nullable
    public final String icon;

    @Nullable
    public final Class<?> editor;

    public TypeInfo(@Nonnull String name,
                    @Nonnull ResolvableType type,
                    @Nullable String title,
                    @Nullable String description,
                    @Nullable String icon,
                    @Nullable Class<?> editor) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.editor = editor;
    }

    protected void map(BiConsumer<String, Object> consumer) {
        consumer.accept("name", name);
        consumer.accept("type", type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
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
        final TypeInfo that = (TypeInfo) obj;
        final Type thisType = this.type.getType();
        final Type thatType = that.type.getType();
        return thisType.equals(thatType);
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map(map::put);
        return getClass().getSimpleName() + map;
    }
}
