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
import java.util.*;

import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.of;
import static org.springframework.core.ResolvableType.NONE;
import static org.springframework.core.ResolvableType.forType;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class BeanIntrospector {

    @Nonnull
    @SuppressWarnings("unchecked")
    public static List<ClassInfo> classInfos(@Nonnull ClassLoader classLoader, @Nonnull ResolvableType type) {
        final List<ClassInfo> classInfos = new ArrayList<>();
        for (final BeanEditor beanEditor : ServiceLoader.load(BeanEditor.class, classLoader)) {
            final ResolvableType beanEditorType = ResolvableType.forClass(BeanEditor.class, beanEditor.getClass());
            final ResolvableType p = beanEditorType.getGeneric(0);
            if (p == NONE || p.getRawClass() == null || !p.getRawClass().isAssignableFrom(type.getRawClass())) {
                continue;
            }
            final Set<Class<?>> editorClasses = beanEditor.editors(type.getRawClass());
            if (editorClasses.isEmpty()) {
                continue;
            }
            final List<ResolvableType> types = new ArrayList<>();
            types.add(type);
            if (type.hasGenerics()) {
                for (final Class<?> c: editorClasses) {
                    final ResolvableType tc = ResolvableType.forClassWithGenerics(c, type.getGenerics());
                    types.add(tc);
                    for (final Type itf : c.getGenericInterfaces()) {
                        final ResolvableType itfType = ResolvableType.forType(itf, tc);
                        if (itfType.getRawClass().isAnnotationPresent(Info.class)) {
                            types.add(itfType);
                        }
                    }
                }
            } else {
                for (final Class<?> c : editorClasses) {
                    types.add(ResolvableType.forClass(c));
                    for (final Class<?> itf : c.getInterfaces()) {
                        if (itf.isAnnotationPresent(Info.class)) {
                            types.add(ResolvableType.forClass(itf));
                        }
                    }
                }
            }
            classInfos.add(types.stream()
                    .map(BeanIntrospector::classInfo)
                    .reduce(BeanIntrospector::merge)
                    .orElseThrow(IllegalStateException::new));
        }
        return classInfos.isEmpty() ? Collections.emptyList() : classInfos;
    }

    public static ClassInfo merge(@Nonnull ClassInfo c1, @Nonnull ClassInfo c2) {
        return new ClassInfo(
                c1.name,
                c1.type,
                c1.title != null ? c1.title : c2.title,
                c1.description != null ? c1.description : c2.description,
                c1.icon != null ? c1.icon : c2.icon,
                merge(c1.editors, c2.editors),
                of(c1.constructorInfos).map(c -> merge(c, c2.constructorInfos)).toArray(MethodInfo[]::new),
                of(c1.methodInfos).map(m -> merge(m, c2.methodInfos)).toArray(MethodInfo[]::new)
        );
    }

    static List<Class<?>> merge(List<Class<?>> e1, List<Class<?>> e2) {
        if (e1.isEmpty()) {
            return e2;
        }
        if (e2.isEmpty()) {
            return e1;
        }
        final List<Class<?>> result = new ArrayList<>(e1.size() + e2.size());
        result.addAll(e1);
        result.addAll(e2);
        return result;
    }

    private static TypeInfo merge(@Nonnull TypeInfo i1, @Nonnull TypeInfo i2) {
        return new TypeInfo(
                i1.name,
                i1.type,
                i2.title != null ? i2.title : i1.title,
                i2.description != null ? i2.description : i1.description,
                i2.icon != null ? i2.icon : i1.icon,
                merge(i1.editors, i2.editors));
    }

    private static TypeInfo[] merge(TypeInfo[] p1, TypeInfo[] p2) {
        return range(0, p1.length).mapToObj(i -> merge(p1[i], p2[i])).toArray(TypeInfo[]::new);
    }

    private static MethodInfo merge(@Nonnull MethodInfo m1, @Nonnull MethodInfo m2) {
        return new MethodInfo(
                m1.name,
                m1.type,
                m1.title != null ? m1.title : m2.title,
                m1.description != null ? m1.description : m2.description,
                m1.icon != null ? m1.icon : m2.icon,
                merge(m1.editors, m2.editors),
                merge(m1.parameters, m2.parameters));
    }

    private static boolean methodEquals(@Nonnull MethodInfo m1, @Nonnull MethodInfo m2) {
        if (m1.name.equals(m2.name)) {
            final Class<?>[] t1 = of(m1.parameters).map(t -> t.type.getRawClass()).toArray(Class[]::new);
            final Class<?>[] t2 = of(m2.parameters).map(t -> t.type.getRawClass()).toArray(Class[]::new);
            return Arrays.equals(t1, t2);
        } else {
            return false;
        }
    }

    private static MethodInfo merge(@Nonnull MethodInfo methodInfo, @Nonnull MethodInfo[] methodInfos) {
        return of(methodInfos).filter(i -> methodEquals(methodInfo, i)).reduce(methodInfo, BeanIntrospector::merge);
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

    private static List<Class<?>> editor(Info info) {
        return info == null || info.editors().length == 0 ? Collections.emptyList() : Arrays.asList(info.editors());
    }
}
