package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanNotFoundException extends RuntimeException {

    public MaridBeanNotFoundException(String beanName) {
        super(beanName);
    }
}
