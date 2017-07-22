/*-
 * #%L
 * marid-util
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.annotation;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov
 */
public class MetaLiteral {

    public final String group;
    public final String name;
    public final String icon;
    public final String description;

    public MetaLiteral(String group, String name, String icon, String description) {
        this.group = group;
        this.name = name;
        this.icon = icon;
        this.description = description;
    }

    public MetaLiteral(@Nonnull MetaInfo metaInfo) {
        this(metaInfo.group(), metaInfo.name(), metaInfo.icon(), metaInfo.description());
    }

    public MetaLiteral(@Nonnull Annotation annotation) {
        final Class<? extends Annotation> type = annotation.annotationType();
        try {
            group = type.getMethod("group").invoke(annotation).toString();
            name = type.getMethod("name").invoke(annotation).toString();
            icon = type.getMethod("icon").invoke(annotation).toString();
            description = type.getMethod("description").invoke(annotation).toString();
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else  if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final MetaLiteral that = (MetaLiteral) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(icon, that.icon) &&
                    Objects.equals(description, that.description);
        }
    }

    public static MetaLiteral l(String group, String name, String icon, String description) {
        return new MetaLiteral(group, name, icon, description);
    }

    @SafeVarargs
    public static <E extends AnnotatedElement & Member> MetaLiteral l(@Nonnull String metaType,
                                                                      @Nonnull String group,
                                                                      @Nonnull String name,
                                                                      @Nonnull String icon,
                                                                      @Nonnull E... elements) {
        final MetaLiteral[] v = of(elements)
                .flatMap(e -> of(e.getDeclaringClass().getPackage(), e.getDeclaringClass(), e))
                .flatMap(Stream::of)
                .flatMap(e -> of(e.getAnnotations()))
                .filter(a -> a.annotationType().isAnnotationPresent(MetaInfoType.class))
                .filter(a -> a.annotationType().getAnnotation(MetaInfoType.class).value().equals(metaType))
                .map(MetaLiteral::new)
                .toArray(MetaLiteral[]::new);
        return new MetaLiteral(
                of(v).map(e -> e.group).filter(e -> !e.isEmpty()).reduce((a, b) -> b).orElse(group),
                of(v).map(e -> e.name).filter(e -> !e.isEmpty()).reduce((a, b) -> b).orElse(name),
                of(v).map(e -> e.icon).filter(e -> !e.isEmpty()).reduce((a, b) -> b).orElse(icon),
                of(v).map(e -> e.description).filter(e -> !e.isEmpty()).reduce((a, b) -> b).orElse("")
        );
    }

    @SafeVarargs
    public static <E extends AnnotatedElement & Member> MetaLiteral l(@Nonnull String metaType,
                                                                      @Nonnull String group,
                                                                      @Nonnull Class<?> type,
                                                                      @Nonnull String icon,
                                                                      @Nonnull E... elements) {
        return l(metaType, group, type.getSimpleName(), icon, elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, description);
    }

    @Override
    public String toString() {
        return String.format("Meta(%s,%s,%s,%s)", group, name, icon, description);
    }
}
