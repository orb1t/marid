package org.marid.spring.annotation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE, FIELD, PARAMETER, ANNOTATION_TYPE})
@Qualifier
@Bean
public @interface IdeAction {
}
