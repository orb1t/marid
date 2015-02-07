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

import org.marid.logging.LogSupport;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.actions.WindowAction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingBeanPostProcessor implements BeanPostProcessor, LogSupport {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (bean instanceof Window && bean.getClass().isAnnotationPresent(PrototypeComponent.class)) {
                final Window window = (Window) bean;
                window.addWindowListener(new WindowAction(e -> {
                    if (e.getID() == WindowEvent.WINDOW_CLOSED) {
                        autowireCapableBeanFactory.destroyBean(bean);
                    }
                }));
            }
        } catch (Exception x) {
            warning("Unable to pre-init bean {0}", x, beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
