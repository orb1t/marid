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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.Ordered;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridCommonPostProcessor implements DestructionAwareBeanPostProcessor, Ordered {

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
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
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
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
