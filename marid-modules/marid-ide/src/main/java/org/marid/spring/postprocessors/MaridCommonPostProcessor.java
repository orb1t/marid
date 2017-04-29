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

package org.marid.spring.postprocessors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridCommonPostProcessor implements DestructionAwareBeanPostProcessor, Ordered {

    public MaridCommonPostProcessor() {
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        log(INFO, "Destructing {0}", beanName);
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName != null) {
            log(INFO, "Initialized {0}", beanName);
        } else {
            log(INFO, "Initialized {0}", bean);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    public static void sort(Collection<InjectedElement> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }
        final List<InjectedElement> list = new ArrayList<>(elements);
        list.sort((ie1, ie2) -> {
            if (ie1.getMember() instanceof AnnotatedElement && ie2.getMember() instanceof AnnotatedElement) {
                final AnnotatedElement e1 = (AnnotatedElement) ie1.getMember();
                final AnnotatedElement e2 = (AnnotatedElement) ie2.getMember();
                final Order o1 = e1.getAnnotation(Order.class);
                final Order o2 = e2.getAnnotation(Order.class);
                if (o1 != null && o2 != null) {
                    return Integer.compare(o1.value(), o2.value());
                } else if (o1 == null && o2 == null) {
                    return ie1.getMember().getName().compareTo(ie2.getMember().getName());
                } else {
                    final int i1 = o1 != null ? o1.value() : Ordered.LOWEST_PRECEDENCE;
                    final int i2 = o2 != null ? o2.value() : Ordered.LOWEST_PRECEDENCE;
                    return Integer.compare(i1, i2);
                }
            } else if (ie1.getMember() instanceof AnnotatedElement) {
                return -1;
            } else if (ie2.getMember() instanceof AnnotatedElement) {
                return 1;
            } else {
                return ie1.getMember().getName().compareTo(ie2.getMember().getName());
            }
        });
        elements.clear();
        elements.addAll(list);
    }
}
