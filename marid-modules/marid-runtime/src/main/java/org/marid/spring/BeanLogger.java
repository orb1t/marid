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

package org.marid.spring;

import org.marid.logging.LogSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BeanLogger implements DestructionAwareBeanPostProcessor, LogSupport {

    private final Logger logger;

    public BeanLogger(Logger logger) {
        this.logger = logger;
    }

    public BeanLogger(String logger) {
        this(Logger.getLogger(logger));
    }

    public BeanLogger() {
        this(LogSupport.LOGGERS.get(BeanLogger.class));
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        info("Destructing {0}", beanName);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        info("Initialized {0}", beanName);
        return bean;
    }
}
