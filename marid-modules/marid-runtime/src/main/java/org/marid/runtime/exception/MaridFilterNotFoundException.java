package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridFilterNotFoundException extends RuntimeException {

    private final String beanName;
    private final String methodName;
    private final String argName;
    private final String filterName;

    public MaridFilterNotFoundException(String beanName, String methodName, String argName, String filterName) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.argName = argName;
        this.filterName = filterName;
    }

    public MaridFilterNotFoundException(String beanName, String methodName, String argName, String filterName, Throwable cause) {
        super(cause);
        this.beanName = beanName;
        this.methodName = methodName;
        this.argName = argName;
        this.filterName = filterName;
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

    public String getFilterName() {
        return filterName;
    }

    @Override
    public String getMessage() {
        return String.format("%s.%s(%s#%s)", beanName, methodName, argName, filterName);
    }
}
