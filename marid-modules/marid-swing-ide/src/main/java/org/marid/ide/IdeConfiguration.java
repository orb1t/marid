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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.spel.SpelParserConfiguration;

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
    public static ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
