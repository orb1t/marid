package org.marid.runtime.beans;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanFactory {

    @Nonnull
    public final String type;

    @Nonnull
    public final String value;

    public BeanFactory(@Nonnull String type, @Nonnull String value) {
        this.type = type;
        this.value = value;
    }

    public BeanFactory(@Nonnull Element element) {
        this.type = requireNonNull(element.getAttribute("ft"));
        this.value = requireNonNull(element.getAttribute("fv"));
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("ft", type);
        element.setAttribute("fv", value);
    }

    @Override
    public String toString() {
        return String.format("Factory(%s,%s)", type, value);
    }
}
