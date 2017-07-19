package org.marid.runtime.context;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridContextException extends RuntimeException {

    public MaridContextException(String message, List<Throwable> suppressed) {
        super(message, null, true, false);
        suppressed.forEach(this::addSuppressed);
    }
}
