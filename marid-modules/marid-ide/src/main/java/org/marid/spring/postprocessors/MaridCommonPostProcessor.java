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
