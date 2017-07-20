package org.marid.runtime.context;

import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public class CircularBeanReferenceException extends RuntimeException {

    public CircularBeanReferenceException(Set<String> current, String name) {
        super(String.format("Circular bean reference: %s/%s", name, current));
    }
}
