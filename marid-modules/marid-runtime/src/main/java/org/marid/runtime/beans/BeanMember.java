package org.marid.runtime.beans;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanMember {

    @Nonnull
    public final String type;

    @Nonnull
    public final String name;

    @Nullable
    public final String value;

    public BeanMember(@Nonnull String type, @Nonnull String name, @Nullable String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public BeanMember(@Nonnull Element element) {
        type = requireNonNull(element.getAttribute("type"));
        name = requireNonNull(element.getAttribute("name"));
        value = element.getTextContent();
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("type", type);
        element.setAttribute("name", name);
        element.setTextContent(value);
    }

    @Override
    public String toString() {
        return String.format("Member(%s,%s,%s)", type, name, value);
    }
}
