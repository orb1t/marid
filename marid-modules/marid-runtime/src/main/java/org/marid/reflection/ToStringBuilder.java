/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.reflection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ToStringBuilder extends ReflectionToStringBuilder {

    private final Predicate<Field> fieldPredicate;

    public ToStringBuilder(Object owner, Predicate<Field> fieldPredicate) {
        super(owner, ToStringStyle.SHORT_PREFIX_STYLE);
        this.fieldPredicate = fieldPredicate;
    }

    public static String toString(Object owner, Class<?>... annotationClasses) {
        final Set<Class<?>> s = new HashSet<>(Arrays.asList(annotationClasses));
        return new ToStringBuilder(owner,
                f -> stream(f.getAnnotations()).anyMatch(a -> s.contains(a.annotationType()))).toString();
    }

    @Override
    protected boolean accept(Field field) {
        return super.accept(field) && fieldPredicate.test(field);
    }
}
