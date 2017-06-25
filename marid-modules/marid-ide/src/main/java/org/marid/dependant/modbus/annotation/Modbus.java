package org.marid.dependant.modbus.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Modbus {

    String value() default "";
}
