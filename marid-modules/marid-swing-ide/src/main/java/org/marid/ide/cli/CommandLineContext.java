/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.cli;

import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.swing.ComponentWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.EnvironmentAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.expression.spel.support.StandardTypeLocator;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 */
public class CommandLineContext extends StandardEvaluationContext implements LogSupport, L10nSupport {

    private final AutowireCapableBeanFactory autowireCapableBeanFactory;
    private final SpelExpressionParser spelExpressionParser;
    private final List<Object> beans = new ArrayList<>();
    private final List<String> beanNames = new ArrayList<>();
    private final ConcurrentMap<String, Object> rootObject = new ConcurrentHashMap<>();

    public CommandLineContext(SpelParserConfiguration spelParserConfiguration, ConfigurableApplicationContext context) {
        autowireCapableBeanFactory = context.getAutowireCapableBeanFactory();
        spelExpressionParser = new SpelExpressionParser(spelParserConfiguration);
        context.getBeanFactory().addBeanPostProcessor(new DestructionListener());
        setRootObject(rootObject);
        addPropertyAccessor(new MapAccessor());
        addPropertyAccessor(new EnvironmentAccessor());
        setBeanResolver((ctx, beanName) -> {
            try {
                final Object bean = context.getBean(beanName);
                if (context.isPrototype(beanName)) {
                    beans.add(bean);
                    beanNames.add(beanName);
                    log(INFO, "Prototype bean {0} was initialized", beanName);
                }
                return bean;
            } catch (BeansException x) {
                throw new AccessException(m("Unable to get bean {0}", beanName), x);
            }
        });
        setTypeLocator(new StandardTypeLocator(context.getBeanFactory().getBeanClassLoader()));
        final ConversionService conversionService = context.getBeanFactory().getConversionService();
        if (conversionService != null) {
            setTypeConverter(new StandardTypeConverter(conversionService));
        }
    }

    public void clean() {
        rootObject.clear();
        for (int i = beans.size() - 1; i >= 0; i--) {
            final Object bean = beans.get(i);
            final String beanName = beanNames.get(i);
            try {
                autowireCapableBeanFactory.destroyBean(bean);
                log(INFO, "Prototype bean {0} was destroyed", beanName);
            } catch (Exception x) {
                log(WARNING, "Unable to destroy bean {0}", x, beanName);
            } finally {
                beans.remove(i);
                beanNames.remove(i);
            }
        }
    }

    public JComponent evaluate(String text, Consumer<Exception> exceptionConsumer) {
        try {
            final Expression expression = spelExpressionParser.parseExpression(text);
            final Object result = expression.getValue(this);
            if (result instanceof ComponentWrapper) {
                return ((ComponentWrapper) result).getWrappedComponent();
            } else {
                final TypeDescriptor source = TypeDescriptor.forObject(result);
                final TypeDescriptor target = TypeDescriptor.valueOf(String.class);
                if (getTypeConverter().canConvert(source, target)) {
                    return new CommandLineResultArea((String) getTypeConverter().convertValue(result, source, target));
                } else if (result.getClass().isArray()) {
                    return new CommandLineResultArea(Arrays.deepToString((Object[]) result));
                } else {
                    return new CommandLineResultArea(result.toString());
                }
            }
        } catch (Exception x) {
            exceptionConsumer.accept(x);
            for (Throwable t = x; t != null; t = t.getCause()) {
                if (t instanceof NoSuchBeanDefinitionException) {
                    final NoSuchBeanDefinitionException exception = (NoSuchBeanDefinitionException) t;
                    return new CommandLineResultArea(m("No such bean {0}", exception.getBeanName()));
                }
            }
            final StringWriter sw = new StringWriter();
            try (final PrintWriter w = new PrintWriter(sw)) {
                x.printStackTrace(w);
            }
            return new CommandLineResultArea(sw.toString());
        }
    }

    class DestructionListener implements DestructionAwareBeanPostProcessor {

        @Override
        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            for (int i = 0; i < beans.size(); i++) {
                if (beans.get(i) == bean) {
                    beanName = beanNames.get(i);
                    beans.remove(i);
                    beanNames.remove(i);
                    rootObject.values().remove(bean);
                    log(INFO, "Removed prototype bean {0}", beanName);
                    break;
                }
            }
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }
}
