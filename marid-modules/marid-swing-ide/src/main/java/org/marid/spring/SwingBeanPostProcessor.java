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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
public class SwingBeanPostProcessor implements BeanPostProcessor, LogSupport {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    boolean isPrototype(String beanName) {
        return beanName == null || autowireCapableBeanFactory.isPrototype(beanName);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (bean instanceof Window && isPrototype(beanName)) {
                final Window window = (Window) bean;
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        window.removeWindowListener(this);
                        autowireCapableBeanFactory.destroyBean(bean);
                    }
                });
            } else if (bean instanceof JInternalFrame && isPrototype(beanName)) {
                final JInternalFrame frame = (JInternalFrame) bean;
                frame.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosed(InternalFrameEvent e) {
                        frame.removeInternalFrameListener(this);
                        autowireCapableBeanFactory.destroyBean(bean);
                    }
                });
            }
        } catch (Exception x) {
            log(WARNING, "Unable to pre-init bean {0}", x, beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof JComponent && isPrototype(beanName)) {
            final JComponent component = (JComponent) bean;
            component.addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    component.removeAncestorListener(this);
                    autowireCapableBeanFactory.destroyBean(component);
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }
        return bean;
    }
}
