/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.spring.postprocessors;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowAndDialogPostProcessor implements BeanPostProcessor {

	private final List<Dialog<?>> dialogs = new ArrayList<>();
	private final List<Window> windows = new ArrayList<>();
	private final AnnotationConfigApplicationContext context;

	public WindowAndDialogPostProcessor(@Nonnull AnnotationConfigApplicationContext context) {
		this.context = context;
	}

	@Override
	public Object postProcessBeforeInitialization(@Nullable Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
		if (beanName != null) {
			if (bean instanceof Dialog<?> && context.isSingleton(beanName)) {
				final Dialog<?> dialog = (Dialog<?>) bean;
				dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
					if (!newValue) {
						dialogs.remove(dialog);
						closeIfNecessary();
					}
				});
				dialogs.add(dialog);
			} else if (bean instanceof Window && context.isSingleton(beanName)) {
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

	private void closeIfNecessary() {
		if (dialogs.isEmpty() && windows.isEmpty()) {
			context.close();
		}
	}
}
