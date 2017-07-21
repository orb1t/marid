package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanMethodArgException extends RuntimeException {

    private final String beanName;
    private final String methodName;
    private final String argName;

    public MaridBeanMethodArgException(String beanName, String methodName, String argName, Throwable cause) {
        super(cause);
        this.beanName = beanName;
        this.methodName = methodName;
        this.argName = argName;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getArgName() {
        return argName;
    }

    @Override
    public String getMessage() {
        return beanName + "." + methodName + "(" + argName + ")";
    }
}
