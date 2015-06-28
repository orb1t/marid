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

package org.marid.ide.context;

import org.marid.ide.log.LoggingPostProcessor;
import org.marid.spring.SwingBeanPostProcessor;
import org.marid.xml.XmlPersister;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BaseContext implements ApplicationContextAware {

    public static GenericApplicationContext context;

    @Bean
    public static LoggingPostProcessor beanPostProcessor() {
        return new LoggingPostProcessor();
    }

    @Bean
    public static SwingBeanPostProcessor swingBeanPostProcessor() {
        return new SwingBeanPostProcessor();
    }

    @Bean
    public static ActionMap ideActionMap() {
        return new ActionMap();
    }

    @Bean
    public static XmlPersister xmlPersister() {
        return new XmlPersister(context);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = (GenericApplicationContext) applicationContext;
    }
}
