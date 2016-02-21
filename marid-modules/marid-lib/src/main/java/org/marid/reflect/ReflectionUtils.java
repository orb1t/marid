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

package org.marid.reflect;

import org.marid.cache.MaridClassValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.reflect.Modifier.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class ReflectionUtils {

    private static final ClassValue<Field[]> FIELDS = new ClassValue<Field[]>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            if (type.isInterface()) {
                return new Field[0];
            }
            final List<Field> fields = new ArrayList<>();
            collect(type, fields);
            return fields.toArray(new Field[fields.size()]);
        }

        private void collect(Class<?> type, List<Field> fields) {
            if (type.getSuperclass() != null) {
                collect(type.getSuperclass(), fields);
            }
            for (final Field field : type.getDeclaredFields()) {
                final int mods = field.getModifiers();
                if (isStatic(mods) || isTransient(mods) || isVolatile(mods)) {
                    continue;
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                fields.add(field);
            }
        }
    };

    private static final ClassValue<Field[]> DECLARED_FIELDS = new MaridClassValue<>(c -> {
        final Field[] fields = c.getDeclaredFields();
        for (final Field field : fields) {
            field.setAccessible(true);
        }
        return fields;
    });

    private static Class<?> declaringClass(Class<?> type, String method, Class<?>... types) {
        try {
            return type.getMethod(method, types).getDeclaringClass();
        } catch (Exception x) {
            throw new IllegalArgumentException(x);
        }
    }

    public static int hashCode(Object object) {
        if (object == null) {
            return 0;
        } else if (!(object instanceof HET) && declaringClass(object.getClass(), "hashCode") != Object.class) {
            return object.hashCode();
        } else if (object.getClass().isArray()) {
            int result = 1;
            final int n = Array.getLength(object);
            for (int i = 0; i < n; i++) {
                result = result * 31 + hashCode(Array.get(object, i));
            }
            return result;
        } else {
            int result = 1;
            for (final Field field : FIELDS.get(object.getClass())) {
                try {
                    result = result * 31 + hashCode(field.get(object));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
            return result;
        }
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        } else if (!(o1 instanceof HET) && o1.equals(o2)) {
            return true;
        } else {
            for (final Field field : FIELDS.get(o1.getClass())) {
                try {
                    if (!equals(field.get(o1), field.get(o2))) {
                        return false;
                    }
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
            return true;
        }
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        } else if (!(object instanceof HET) && declaringClass(object.getClass(), "toString") != Object.class) {
            return object.toString();
        } else if (object.getClass().isArray()) {
            final int n = Array.getLength(object);
            final String[] result = new String[n];
            for (int i = 0; i < n; i++) {
                result[i] = toString(Array.get(object, i));
            }
            return Arrays.toString(result);
        } else {
            final Map<String, Object> map = new LinkedHashMap<>();
            for (final Field field : FIELDS.get(object.getClass())) {
                try {
                    map.put(field.getName(), toString(field.get(object)));
                } catch (ReflectiveOperationException x) {
                    throw new IllegalStateException(x);
                }
            }
            return object.getClass().getSimpleName() + map;
        }
    }

    public static <A extends Annotation> void visitAnnotations(Class<?> target, Class<A> type, Consumer<A> consumer) {
        for (final A annotation : target.getAnnotationsByType(type)) {
            consumer.accept(annotation);
        }
        for (Class<?> c = target.getSuperclass(); c != null; c = c.getSuperclass()) {
            for (final A annotation : c.getAnnotationsByType(type)) {
                consumer.accept(annotation);
            }
        }
        for (final Class<?> i : target.getInterfaces()) {
            for (final A annotation : i.getAnnotationsByType(type)) {
                consumer.accept(annotation);
            }
        }
    }

    public static <A extends Annotation> TreeSet<A> annotations(Class<?> target, Class<A> type, Comparator<A> comparator) {
        final TreeSet<A> set = new TreeSet<>(comparator);
        visitAnnotations(target, type, set::add);
        return set;
    }

    public static Field[] getFields(Class<?> type) {
        return FIELDS.get(type);
    }

    public static Field[] getDeclaredFields(Class<?> type) {
        return DECLARED_FIELDS.get(type);
    }

    public static class HET {

        @Override
        public int hashCode() {
            return ReflectionUtils.hashCode(this);
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            return ReflectionUtils.equals(this, obj);
        }

        @Override
        public String toString() {
            return ReflectionUtils.toString(this);
        }
    }
}
