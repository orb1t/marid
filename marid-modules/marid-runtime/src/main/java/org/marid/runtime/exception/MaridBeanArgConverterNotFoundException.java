package org.marid.runtime.exception;

import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanArgConverterNotFoundException extends RuntimeException {

    private final String beanName;
    private final String methodName;
    private final String argName;
    private final String type;

    public MaridBeanArgConverterNotFoundException(String beanName, String methodName, String argName, String type) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.argName = argName;
        this.type = type;
    }

    public MaridBeanArgConverterNotFoundException(Bean bean, BeanMethod method, BeanMethodArg arg) {
        this(bean.name, method.name(), arg.name, arg.type);
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

    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return String.format("%s.%s(%s:%s)", beanName, methodName, argName, type);
    }
}
