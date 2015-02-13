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

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpringUtils {

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
}
