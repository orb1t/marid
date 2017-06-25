package org.marid.spring.dependant;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import javax.annotation.Resource;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public abstract class DependantConfiguration<T> {

    protected T param;

    @SuppressWarnings("unchecked")
    @Resource
    private void init(ApplicationContext context) {
        final ResolvableType type = ResolvableType.forClass(DependantConfiguration.class, getClass());
        final ResolvableType generic = type.getGeneric(0);
        final Class<?> c = generic.getRawClass();
        param = (T) context.getBean(c);
    }
}
