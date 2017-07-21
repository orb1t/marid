package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridMethodNotFoundException extends RuntimeException {

    private final String beanName;
    private final String methodName;

    public MaridMethodNotFoundException(String beanName, String methodName, Throwable cause) {
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
