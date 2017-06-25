package org.marid.spring.annotation;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dmitry Ovchinnikov
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Bean
public @interface PrototypeBean {

    String[] value() default {};
}
