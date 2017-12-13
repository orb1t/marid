/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.idelib.spring.postprocessors;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowAndDialogPostProcessor implements BeanPostProcessor {

  private final List<Dialog<?>> dialogs = new ArrayList<>();
  private final List<Window> windows = new ArrayList<>();
  private final AnnotationConfigApplicationContext context;

  public WindowAndDialogPostProcessor(@NotNull AnnotationConfigApplicationContext context) {
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
