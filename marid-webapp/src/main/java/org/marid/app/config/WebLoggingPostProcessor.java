/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

package org.marid.app.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class WebLoggingPostProcessor implements BeanFactoryPostProcessor {

  private final Logger logger = LoggerFactory.getLogger("marid");

  @Override
  public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
    beanFactory.addBeanPostProcessor(new DestructionAwareBeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(@Nullable Object bean, @Nullable String beanName) {
        if (beanName != null) {
          logger.info("Initializing {}", beanName);
        }
        return bean;
      }

      @Override
      public void postProcessBeforeDestruction(@Nullable Object bean, @Nullable String beanName) {
        if (beanName != null) {
          logger.info("Destroyed {}", beanName);
        }
      }
    });
  }
}
