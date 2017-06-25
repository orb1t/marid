package org.marid.spring.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Dmitry Ovchinnikov
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Scope(SCOPE_PROTOTYPE)
@Component
public @interface PrototypeComponent {

    String value() default "";
}
