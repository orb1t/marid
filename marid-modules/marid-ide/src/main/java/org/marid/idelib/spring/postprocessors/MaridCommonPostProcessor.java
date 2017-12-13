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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.Ordered;

import org.jetbrains.annotations.Nullable;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridCommonPostProcessor implements DestructionAwareBeanPostProcessor, Ordered {

  @Override
  public void postProcessBeforeDestruction(@Nullable Object bean, @Nullable String beanName) throws BeansException {
    if (beanName == null) {
      if (bean != null) {
        try {
          log(INFO, "Destructing {0}", bean);
        } catch (Exception x) {
          log(INFO, "Destructing {0}", bean.getClass().getSimpleName());
        }
      }
    } else {
      log(INFO, "Destructing {0}", beanName);
    }
  }

  @Override
  public boolean requiresDestruction(Object bean) {
    return true;
  }

  @Override
  public Object postProcessBeforeInitialization(@Nullable Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
    if (beanName != null) {
      log(INFO, "Initialized {0}", beanName);
    } else {
      log(INFO, "Initialized {0}", bean);
    }
    return bean;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
