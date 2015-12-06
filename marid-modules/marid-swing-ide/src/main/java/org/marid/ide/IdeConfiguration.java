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

package org.marid.ide;

import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.expression.spel.support.StandardTypeLocator;

import javax.swing.*;

import static org.springframework.expression.spel.SpelCompilerMode.MIXED;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class IdeConfiguration {

    @Bean
    public static ActionMap ideActionMap() {
        return new ActionMap();
    }

    @Bean
    public static SpelParserConfiguration spelParserConfiguration(ConfigurableApplicationContext context) {
        return new SpelParserConfiguration(MIXED, context.getClassLoader());
    }

    @Bean
    public static StandardEvaluationContext evaluationContext(ConfigurableApplicationContext context) {
        final BeanExpressionContext expressionContext = new BeanExpressionContext(context.getBeanFactory(), null);
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setRootObject(expressionContext);
        evaluationContext.addPropertyAccessor(new BeanExpressionContextAccessor());
        evaluationContext.addPropertyAccessor(new BeanFactoryAccessor());
        evaluationContext.addPropertyAccessor(new MapAccessor());
        evaluationContext.addPropertyAccessor(new EnvironmentAccessor());
        evaluationContext.setBeanResolver(new BeanFactoryResolver(context.getBeanFactory()));
        evaluationContext.setTypeLocator(new StandardTypeLocator(context.getBeanFactory().getBeanClassLoader()));
        final ConversionService conversionService = context.getBeanFactory().getConversionService();
        if (conversionService != null) {
            evaluationContext.setTypeConverter(new StandardTypeConverter(conversionService));
        }
        return evaluationContext;
    }
}
