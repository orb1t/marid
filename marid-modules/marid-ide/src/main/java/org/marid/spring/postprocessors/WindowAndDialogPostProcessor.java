/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.spring.postprocessors;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowAndDialogPostProcessor implements BeanPostProcessor {

    private final List<Dialog<?>> dialogs = new ArrayList<>();
    private final List<Window> windows = new ArrayList<>();
    private final AnnotationConfigApplicationContext context;

    public WindowAndDialogPostProcessor(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName != null && matches(bean) && context.isSingleton(beanName)) {
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

    private static boolean matches(Object bean) {
        return bean instanceof Dialog || bean instanceof Window;
    }

    private void closeIfNecessary() {
        if (dialogs.isEmpty() && windows.isEmpty()) {
            context.close();
        }
    }
}
