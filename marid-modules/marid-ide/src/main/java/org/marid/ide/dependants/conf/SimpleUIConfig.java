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

package org.marid.ide.dependants.conf;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleUIConfig implements DestructionAwareBeanPostProcessor {

    private final List<Dialog<?>> dialogs = new ArrayList<>();
    private final List<Window> windows = new ArrayList<>();
    private final AnnotationConfigApplicationContext context;

    public SimpleUIConfig(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (context != null && context.isSingleton(beanName)) {
            if (bean instanceof Dialog<?>) {
                final Dialog<?> dialog = (Dialog<?>) bean;
                dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        dialogs.remove(dialog);
                        closeIfNecessary();
                    }
                });
                dialogs.add(dialog);
            } else if (bean instanceof Window) {
                final Window window = (Window) bean;
                window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
                    windows.remove(window);
                    closeIfNecessary();
                });
                windows.add(window);
            }
        }
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof Dialog<?>) {
            dialogs.remove(bean);
            ((Dialog<?>) bean).close();
        } else if (bean instanceof Window) {
            windows.remove(bean);
            ((Window) bean).hide();
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    private void closeIfNecessary() {
        if (dialogs.isEmpty() && windows.isEmpty()) {
            context.close();
        }
    }
}
