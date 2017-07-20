package org.marid.runtime.context;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridRuntime {

    Object getBean(String name);

    boolean isActive();

    ClassLoader getClassLoader();
}
