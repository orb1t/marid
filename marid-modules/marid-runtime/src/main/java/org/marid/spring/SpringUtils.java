/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import groovy.lang.Closure;
import org.marid.Marid;
import org.marid.itf.Named;
import org.marid.pref.PrefCodecs;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.marid.util.Utils.cast;
import static org.marid.util.Utils.currentClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpringUtils {

    private static final AnnotationBeanNameGenerator DEFAULT_BEAN_NAME_GENERATOR = new AnnotationBeanNameGenerator();
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) field.get(null);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T resolve(String value, Class<T> type, ConfigurableApplicationContext context) {
        final String resolvedValue = context.getEnvironment().resolvePlaceholders(value);
        final ConfigurableBeanFactory beanFactory = context.getBeanFactory();
        final BeanExpressionContext expressionContext = new BeanExpressionContext(beanFactory, null);
        final BeanExpressionResolver expressionResolver = beanFactory.getBeanExpressionResolver();
        if (expressionResolver == null) {
            return beanFactory.getTypeConverter().convertIfNecessary(value, type);
        } else {
            final Object result = expressionResolver.evaluate(resolvedValue, expressionContext);
            return beanFactory.getTypeConverter().convertIfNecessary(result, type);
        }
    }

    public static <T> T parse(Class<?> target, Class<T> itf) {
        final ConfigurableEnvironment env = Marid.getCurrentContext().getEnvironment();
        return cast(Proxy.newProxyInstance(currentClassLoader(), new Class<?>[]{itf}, (proxy, method, args) -> {
            if (!method.isDefault()) {
                return null;
            }
            Object value = null;
            if (args != null && args.length > 0 && args[0] instanceof Named) {
                value = env.getProperty(((Named) args[0]).getName() + "." + method.getName(), Object.class);
            }
            if (value == null) {
                value = env.getProperty(itf.getSimpleName() + "." + method.getName(), Object.class);
            }
            for (Class<?> c = target; c != null && value == null; c = c.getSuperclass()) {
                value = env.getProperty(c.getSimpleName() + "." + method.getName(), Object.class);
            }
            if (value == null) {
                return IMPL_LOOKUP
                        .unreflectSpecial(method, method.getDeclaringClass())
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            } else {
                if (value instanceof Closure) {
                    value = ((Closure) value).call(args);
                }
                return PrefCodecs.castTo(value, method.getReturnType());
            }
        }));
    }

    public static String beanName(Class<?> target) {
        final AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(target);
        return DEFAULT_BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, null);
    }
}
