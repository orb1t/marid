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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ClassInfo extends TypeInfo {

    @Nonnull
    public final MethodInfo[] constructorInfos;

    @Nonnull
    public final MethodInfo[] methodInfos;

    @Nonnull
    public final TypeInfo[] propertyInfos;

    public ClassInfo(@Nonnull String name,
                     @Nonnull ResolvableType type,
                     @Nullable String title,
                     @Nullable String description,
                     @Nullable String icon,
                     @Nonnull List<Class<?>> editors,
                     @Nonnull MethodInfo[] constructorInfos,
                     @Nonnull MethodInfo[] methodInfos) {
        super(name, type, title, description, icon, editors);
        this.constructorInfos = constructorInfos;
        this.methodInfos = methodInfos;

        final Map<String, MethodInfo[]> properties = new LinkedHashMap<>();
        for (final MethodInfo methodInfo : methodInfos) {
            final String propertyName = methodInfo.getPropertyName();
            if (propertyName != null) {
                final MethodInfo[] methods = properties.computeIfAbsent(propertyName, n -> new MethodInfo[2]);
                methods[methodInfo.isGetter() ? 0 : 1] = methodInfo;
            }
        }
        propertyInfos = properties.entrySet().stream()
                .map(e -> merge(e.getKey(), e.getValue()))
                .toArray(TypeInfo[]::new);
    }

    @Override
    protected void map(BiConsumer<String, Object> consumer) {
        super.map(consumer);
        consumer.accept("constructors", of(constructorInfos).collect(toCollection(LinkedHashSet::new)));
        consumer.accept("methods", of(methodInfos).collect(toCollection(LinkedHashSet::new)));
        consumer.accept("properties", of(propertyInfos).collect(toCollection(LinkedHashSet::new)));
    }

    private static TypeInfo merge(String pName, MethodInfo[] methodInfos) {
        final ResolvableType pType = methodInfos[0] != null ? methodInfos[0].type : methodInfos[1].type;
        final Function<Function<MethodInfo, String>, String> func = f -> {
            if (methodInfos[0] != null && f.apply(methodInfos[0]) != null) {
                return f.apply(methodInfos[0]);
            } else if (methodInfos[1] != null && f.apply(methodInfos[1]) != null) {
                return f.apply(methodInfos[1]);
            } else {
                return null;
            }
        };
        final String pTitle = func.apply(i -> i.title);
        final String pDesc = func.apply(i -> i.description);
        final String pIcon = func.apply(i -> i.icon);
        final List<List<Class<?>>> editorList = Arrays.asList(
                methodInfos[0] != null ? methodInfos[0].editors : Collections.emptyList(),
                methodInfos[1] != null ? methodInfos[1].editors : Collections.emptyList()
        );
        final List<Class<?>> editors = BeanIntrospector.merge(editorList.get(0), editorList.get(1));
        return new TypeInfo(pName, pType, pTitle, pDesc, pIcon, editors);
    }
}
