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

import javafx.beans.NamedArg;
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.of;
import static org.springframework.core.ResolvableType.forType;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class BeanIntrospector {

    @Nonnull
    public static ClassInfo classInfo(@Nonnull ClassLoader classLoader, @Nonnull ResolvableType type) {
        final List<ResolvableType> types = new ArrayList<>(Collections.singletonList(type));
        final Class<?> descriptorClass = loadClass(classLoader, type.getRawClass().getName() + "Descriptor");
        if (descriptorClass != null) {
            final ResolvableType descriptorType = type.hasGenerics()
                    ? ResolvableType.forClassWithGenerics(descriptorClass, type.getGenerics())
                    : ResolvableType.forClass(descriptorClass);
            if (type.isAssignableFrom(descriptorType)) {
                types.add(descriptorType);
                for (final Type itf : descriptorClass.getGenericInterfaces()) {
                    final ResolvableType itfType = ResolvableType.forType(itf);
                    if (itfType.getRawClass().isAnnotationPresent(Info.class)) {
                        types.add(ResolvableType.forType(itf, descriptorType));
                    }
                }
            }
        }
        final List<ClassInfo> classInfos = types.stream().map(BeanIntrospector::classInfo).collect(toList());
        final ClassInfo head = classInfos.get(0);
        final List<ClassInfo> tail = classInfos.subList(1, classInfos.size());
        final String title = classInfos.stream().map(i -> i.title).filter(Objects::nonNull).findAny().orElse(null);
        final String desc = classInfos.stream().map(i -> i.description).filter(Objects::nonNull).findAny().orElse(null);
        final String icon = classInfos.stream().map(i -> i.icon).filter(Objects::nonNull).findAny().orElse(null);
        final Class<?> editor = classInfos.stream().map(i -> i.editor).filter(Objects::nonNull).findAny().orElse(null);
        final MethodInfo[] cs = of(head.constructorInfos).map(c -> cm(c, tail)).toArray(MethodInfo[]::new);
        final MethodInfo[] ms = of(head.methodInfos).map(m -> mm(m, tail)).toArray(MethodInfo[]::new);
        return new ClassInfo(head.name, head.type, title, desc, icon, editor, cs, ms);
    }

    private static Class<?> loadClass(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException x) {
            return null;
        }
    }

    private static TypeInfo merge(@Nonnull TypeInfo i1, @Nonnull TypeInfo i2) {
        return new TypeInfo(
                i1.name,
                i1.type,
                i2.title != null ? i2.title : i1.title,
                i2.description != null ? i2.description : i1.description,
                i2.icon != null ? i2.icon : i1.icon,
                i2.editor != null ? i2.editor : i1.editor);
    }

    private static TypeInfo[] merge(TypeInfo[] p1, TypeInfo[] p2) {
        return range(0, p1.length).mapToObj(i -> merge(p1[i], p2[i])).toArray(TypeInfo[]::new);
    }

    private static MethodInfo merge(@Nonnull MethodInfo m1, @Nonnull MethodInfo m2) {
        return new MethodInfo(
                m1.name,
                m1.type,
                m2.title != null ? m2.title : m1.title,
                m2.description != null ? m2.description : m1.description,
                m2.icon != null ? m2.icon : m1.icon,
                m2.editor != null ? m2.editor : m1.editor,
                merge(m1.parameters, m2.parameters));
    }

    private static MethodInfo mm(@Nonnull MethodInfo info, @Nonnull List<ClassInfo> classInfos) {
        return classInfos.stream()
                .flatMap(c -> of(c.methodInfos))
                .filter(m -> m.name.equals(info.name))
                .filter(m -> info.parameters.length == m.parameters.length)
                .filter(m -> range(0, m.parameters.length)
                        .allMatch(i -> m.parameters[i].type.getRawClass() == info.parameters[i].type.getRawClass()))
                .reduce(info, BeanIntrospector::merge);
    }

    private static MethodInfo cm(@Nonnull MethodInfo info, @Nonnull List<ClassInfo> classInfos) {
        return classInfos.stream()
                .flatMap(c -> of(c.constructorInfos))
                .filter(m -> info.parameters.length == m.parameters.length)
                .filter(m -> range(0, m.parameters.length)
                        .allMatch(i -> m.parameters[i].type.getRawClass() == info.parameters[i].type.getRawClass()))
                .reduce(info, BeanIntrospector::merge);
    }

    private static TypeInfo p(@Nonnull Parameter parameter, @Nonnull ResolvableType type) {
        final Info info = parameter.getAnnotation(Info.class);
        final ResolvableType paramType = forType(parameter.getParameterizedType(), type);
        final String name = parameter.isAnnotationPresent(NamedArg.class)
                ? parameter.getAnnotation(NamedArg.class).value()
                : parameter.getName();
        return new TypeInfo(name, paramType, title(info), desc(info), icon(info), editor(info));
    }

    private static MethodInfo c(@Nonnull Constructor<?> constructor, @Nonnull ResolvableType type) {
        final Info info = constructor.getAnnotation(Info.class);
        final TypeInfo[] ps = of(constructor.getParameters()).map(p -> p(p, type)).toArray(TypeInfo[]::new);
        return new MethodInfo("init", type, title(info), desc(info), icon(info), editor(info), ps);
    }

    private static MethodInfo m(@Nonnull Method method, @Nonnull ResolvableType type) {
        final Info info = method.getAnnotation(Info.class);
        final TypeInfo[] ps = of(method.getParameters()).map(p -> p(p, type)).toArray(TypeInfo[]::new);
        final ResolvableType methodType = forType(method.getGenericReturnType(), type);
        return new MethodInfo(method.getName(), methodType, title(info), desc(info), icon(info), editor(info), ps);
    }

    private static ClassInfo classInfo(@Nonnull ResolvableType type) {
        final Class<?> rc = type.getRawClass();
        final Info info = rc.getAnnotation(Info.class);
        final MethodInfo[] cs = of(rc.getConstructors()).map(c -> c(c, type)).toArray(MethodInfo[]::new);
        final MethodInfo[] ms = of(rc.getMethods()).map(m -> m(m, type)).toArray(MethodInfo[]::new);
        return new ClassInfo(rc.getName(), type, title(info), desc(info), icon(info), editor(info), cs, ms);
    }

    private static String title(Info info) {
        return info != null && !info.title().isEmpty() ? info.title() : null;
    }

    private static String desc(Info info) {
        return info != null && !info.description().isEmpty() ? info.description() : null;
    }

    private static String icon(Info info) {
        return info != null && !info.icon().isEmpty() ? info.icon() : null;
    }

    private static Class<?> editor(Info info) {
        return info != null && info.editor() != Object.class ? info.editor() : null;
    }
}
