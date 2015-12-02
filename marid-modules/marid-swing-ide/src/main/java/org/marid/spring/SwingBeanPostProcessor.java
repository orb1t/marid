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
import org.marid.swing.actions.InternalFrameAction;
import org.marid.swing.actions.WindowAction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
public class SwingBeanPostProcessor implements BeanPostProcessor, LogSupport {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    private final Set<Component> prototypeComponents = Collections.newSetFromMap(new IdentityHashMap<>());

    boolean isPrototype(String beanName) {
        return beanName == null || autowireCapableBeanFactory.isPrototype(beanName);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (bean instanceof Window && isPrototype(beanName)) {
                final Window window = (Window) bean;
                window.addWindowListener(new WindowAction(e -> {
                    if (e.getID() == WindowEvent.WINDOW_CLOSED) {
                        destroyGraphicals(window);
                        autowireCapableBeanFactory.destroyBean(bean);
                    }
                }));
            } else if (bean instanceof JInternalFrame && isPrototype(beanName)) {
                final JInternalFrame frame = (JInternalFrame) bean;
                frame.addInternalFrameListener(new InternalFrameAction(e -> {
                    if (e.getID() == InternalFrameEvent.INTERNAL_FRAME_CLOSED) {
                        destroyGraphicals(frame);
                        autowireCapableBeanFactory.destroyBean(bean);
                    }
                }));
            } else if (bean instanceof Component && isPrototype(beanName)) {
                prototypeComponents.add((Component) bean);
            }
        } catch (Exception x) {
            log(WARNING, "Unable to pre-init bean {0}", x, beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private void destroyGraphicals(Container container) {
        destroyGraphicals(container, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private void destroyGraphicals(Container container, Set<Component> components) {
        synchronized (container.getTreeLock()) {
            for (int i = 0; i < container.getComponentCount(); i++) {
                final Component component = container.getComponent(i);
                if (components.contains(component)) {
                    continue;
                }
                components.add(component);
                if (component instanceof Container) {
                    destroyGraphicals((Container) component, components);
                }
                if (prototypeComponents.remove(component)) {
                    autowireCapableBeanFactory.destroyBean(component);
                }
            }
        }
    }
}
