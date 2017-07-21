package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanMethodInvocationException extends RuntimeException {

    private final String beanName;
    private final String methodName;

    public MaridBeanMethodInvocationException(String beanName, String methodName, Throwable cause) {
        super(cause);
        this.beanName = beanName;
        this.methodName = methodName;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getMessage() {
        return beanName + "." + methodName;
    }
}
