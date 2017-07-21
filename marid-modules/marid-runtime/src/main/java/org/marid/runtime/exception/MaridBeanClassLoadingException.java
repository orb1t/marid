package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanClassLoadingException extends RuntimeException {

    private final String beanName;
    private final String className;

    public MaridBeanClassLoadingException(String beanName, String className, Throwable cause) {
        super(cause);
        this.beanName = beanName;
        this.className = className;
    }

    @Override
    public String getMessage() {
        return beanName + "(" + className + ")";
    }

    public String getClassName() {
        return className;
    }

    public String getBeanName() {
        return beanName;
    }
}
