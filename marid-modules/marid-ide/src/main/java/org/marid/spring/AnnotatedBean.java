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

package org.marid.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class AnnotatedBean<T extends Annotation, E> {

    public final T annotation;
    public final E object;
    public final String beanName;
    public final BeanDefinition definition;
    public final AnnotatedTypeMetadata metadata;

    AnnotatedBean(T annotation, E object, String beanName, BeanDefinition definition, AnnotatedTypeMetadata metadata) {
        this.annotation = annotation;
        this.object = object;
        this.beanName = beanName;
        this.definition = definition;
        this.metadata = metadata;
    }

    public <A extends Annotation> A getAnnotation(Class<A> type) {
        final Map<String, Object> map = metadata.getAnnotationAttributes(type.getName(), false);
        return map == null ? null : AnnotationUtils.synthesizeAnnotation(map, type, null);
    }

    public static <T extends Annotation, E> void walk(GenericApplicationContext context, Class<T> annotationType, Class<E> type, Consumer<AnnotatedBean<T, E>> walker) {
        for (final String beanName : context.getBeanDefinitionNames()) {
            final BeanDefinition definition = context.getBeanDefinition(beanName);
            final Object source = definition.getSource();
            if (source instanceof AnnotatedTypeMetadata) {
                final AnnotatedTypeMetadata metadata = (AnnotatedTypeMetadata) source;
                final Map<String, Object> map = metadata.getAnnotationAttributes(annotationType.getName(), false);
                if (map != null) {
                    final T annotation = AnnotationUtils.synthesizeAnnotation(map, annotationType, null);
                    final Object bean = context.getBean(beanName);
                    if (type.isInstance(bean)) {
                        walker.accept(new AnnotatedBean<>(annotation, type.cast(bean), beanName, definition, metadata));
                    }
                }
            }
        }
    }

    public static <T extends Annotation> void walk(GenericApplicationContext context, Class<T> annotationType, Consumer<AnnotatedBean<T, Object>> walker) {
        walk(context, annotationType, Object.class, walker);
    }
}
