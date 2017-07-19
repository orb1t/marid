package org.marid.runtime.beans;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFactory {

    @Nullable
    public final String ref;

    @Nullable
    public final String factoryClass;

    @Nullable
    public final String filter;

    public BeanFactory(@Nonnull String factory) {
        final int filterIndex = factory.lastIndexOf('#');
        if (filterIndex < 0) {
            filter = null;
        } else {
            filter = factory.substring(filterIndex + 1);
            factory = factory.substring(0, filterIndex);
        }
        if (factory.startsWith("@")) {
            ref = factory.substring(1);
            factoryClass = null;
        } else {
            ref = null;
            factoryClass = factory;
        }
    }
}
