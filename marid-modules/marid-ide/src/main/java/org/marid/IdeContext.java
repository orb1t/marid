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

package org.marid;

import org.marid.ide.panes.logging.IdeLogHandler;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.DefaultLifecycleProcessor;

import static org.springframework.context.support.AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ImportResource({"classpath*:/META-INF/marid/**/*.xml"})
@ComponentScan({"org.marid.ide"})
public class IdeContext {

    @Bean
    public Ide application() {
        return Ide.application;
    }

    @Bean
    public IdeLogHandler ideLogHandler() {
        return Ide.ideLogHandler;
    }

    @Bean(name = LIFECYCLE_PROCESSOR_BEAN_NAME)
    public LifecycleProcessor lifecycleProcessor() {
        return new DefaultLifecycleProcessor();
    }
}
