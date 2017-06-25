package org.marid.spring.postprocessors;

import org.marid.misc.Builder;
import org.marid.spring.dependant.IdeClassFilter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeAppContext extends AnnotationConfigApplicationContext {

    public IdeAppContext() {
        super(Builder.build(new DefaultListableBeanFactory(), beanFactory -> {
            IdeAutowirePostProcessor.register(beanFactory);
            beanFactory.registerSingleton("ideClassFiltter", new IdeClassFilter());
            beanFactory.addBeanPostProcessor(new MaridCommonPostProcessor());
        }));
        setAllowBeanDefinitionOverriding(true);
        setAllowCircularReferences(false);
    }
}
