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

package org.marid.runtime;

import org.marid.logging.LogSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.*;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanPostProcessor implements LogSupport, DestructionAwareBeanPostProcessor {

    private static final Logger LOGGER = Logger.getLogger("marid");
    private static final Pattern INIT_ATTR = Pattern.compile("init(\\d+)");
    private static final Pattern DESTROY_ATTR = Pattern.compile("destroy(\\d+)");

    private final GenericApplicationContext context;
    private final MaridBeanExpressionContext beanExpressionContext;

    public MaridBeanPostProcessor(GenericApplicationContext context) {
        this.context = context;
        this.beanExpressionContext = new MaridBeanExpressionContext(context.getBeanFactory());
    }

    @Nonnull
    @Override
    public Logger logger() {
        return LOGGER;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        log(INFO, "Destroying {0}", beanName);
        applyTriggers("destroy", bean, beanName, DESTROY_ATTR);
    }

    @Override
    public boolean requiresDestruction(Object o) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        log(INFO, "Initializing {0}", beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        applyTriggers("init", bean, beanName, INIT_ATTR);
        log(INFO, "Initialized {0}", beanName);
        return bean;
    }

    private void applyTriggers(String type, Object bean, String beanName, Pattern attrPattern) throws BeansException {
        final BeanDefinition beanDefinition;
        try {
            beanDefinition = context.getBeanDefinition(beanName);
        } catch (NoSuchBeanDefinitionException x) {
            throw new BeanInitializationException("No such bean exception", x);
        }
        final String[] attributeNames = beanDefinition.attributeNames();
        if (attributeNames.length == 0) {
            return;
        }
        final Map<Integer, String> indexedAttributes = new TreeMap<>();
        for (final String attr : attributeNames) {
            final Matcher matcher = attrPattern.matcher(attr);
            if (matcher.matches()) {
                final int index = Integer.parseInt(matcher.group(1));
                indexedAttributes.put(index, attr);
            }
        }
        if (indexedAttributes.isEmpty()) {
            return;
        }
        final BeanExpressionResolver resolver = context.getBeanFactory().getBeanExpressionResolver();
        beanExpressionContext.results.put(-1, bean);
        for (final Map.Entry<Integer, String> entry : indexedAttributes.entrySet()) {
            final String attr = entry.getValue();
            entry.setValue(beanDefinition.getAttribute(attr).toString());
        }
        for (final Map.Entry<Integer, String> entry : indexedAttributes.entrySet()) {
            final Integer index = entry.getKey();
            final String expression = entry.getValue();
            try {
                final Object result = resolver.evaluate(expression, beanExpressionContext);
                beanExpressionContext.results.put(index, result);
            } catch (BeansException x) {
                throw new BeanInitializationException(type + " trigger " + index + " error", x);
            }
        }
    }

    private static class MaridBeanExpressionContext extends BeanExpressionContext {

        private final TreeMap<Integer, Object> results = new TreeMap<>();

        public MaridBeanExpressionContext(ConfigurableBeanFactory beanFactory) {
            super(beanFactory, null);
        }

        @Override
        public boolean containsObject(String key) {
            switch (key) {
                case "it":
                case "results":
                    return true;
                default:
                    return super.containsObject(key);
            }
        }

        @Override
        public Object getObject(String key) {
            switch (key) {
                case "it":
                    return results.get(-1);
                case "results":
                    return results;
                default:
                    return super.getObject(key);
            }
        }
    }
}
