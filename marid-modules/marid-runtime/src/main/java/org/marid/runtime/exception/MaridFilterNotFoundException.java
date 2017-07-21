package org.marid.runtime.exception;

import org.marid.runtime.beans.BeanMethodArg;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridFilterNotFoundException extends RuntimeException {

    private final BeanMethodArg arg;

    public MaridFilterNotFoundException(BeanMethodArg arg) {
        this.arg = arg;
    }

    public BeanMethodArg getArg() {
        return arg;
    }
}
