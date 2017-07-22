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

import java.lang.annotation.Annotation;
import java.util.Objects;

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

    public MetaLiteral(MetaInfo metaInfo) {
        this(metaInfo.group(), metaInfo.name(), metaInfo.icon(), metaInfo.description());
    }

    public MetaLiteral(Annotation annotation) {
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

    public MetaLiteral(String name, String icon, String desc, Annotation... annotations) {
        final MetaLiteral[] v = of(annotations).map(MetaLiteral::new).toArray(MetaLiteral[]::new);
        this.group = of(v).map(l -> l.group).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse("");
        this.name = of(v).map(l -> l.name).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(name);
        this.icon = of(v).map(l -> l.icon).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(icon);
        description = of(v).map(l -> l.description).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(desc);
    }

    public MetaLiteral(Class<?> type, String icon, Annotation... annotations) {
        final MetaLiteral[] v = of(annotations).map(MetaLiteral::new).toArray(MetaLiteral[]::new);
        this.group = of(v).map(l -> l.group).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse("");
        this.name = of(v).map(l -> l.name).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(type.getSimpleName());
        this.icon = of(v).map(l -> l.icon).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(icon);
        description = of(v).map(l -> l.description).filter(s -> !s.isEmpty()).reduce((s1, s2) -> s2).orElse(type.getName());
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

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, description);
    }

    @Override
    public String toString() {
        return String.format("Meta(%s,%s,%s)", name, icon, description);
    }
}
