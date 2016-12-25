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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

    @Nonnull
    public final List<Class<?>> editors;

    public TypeInfo(@Nonnull String name,
                    @Nonnull ResolvableType type,
                    @Nullable String title,
                    @Nullable String description,
                    @Nullable String icon,
                    @Nonnull List<Class<?>> editors) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.editors = editors;
    }

    public TypeInfo(@Nonnull String name, @Nonnull ResolvableType type) {
        this(name, type, null, null, null, Collections.emptyList());
    }

    protected void map(BiConsumer<String, Object> consumer) {
        consumer.accept("name", name);
        consumer.accept("type", type);
        if (title != null) {
            consumer.accept("title", title);
        }
        if (description != null) {
            consumer.accept("description", description);
        }
        if (icon != null) {
            consumer.accept("icon", icon);
        }
        if (!editors.isEmpty()) {
            consumer.accept("editors", editors);
        }
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map(map::put);
        return getClass().getSimpleName() + map;
    }
}
