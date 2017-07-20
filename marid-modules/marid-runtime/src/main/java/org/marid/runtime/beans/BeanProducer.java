package org.marid.runtime.beans;

import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

import static org.marid.io.Xmls.nodes;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanProducer {

    @Nonnull
    public final String signature;

    @Nonnull
    public final BeanMember[] args;

    public BeanProducer(@Nonnull String signature, @Nonnull BeanMember... args) {
        this.signature = signature;
        this.args = args;
    }

    public BeanProducer(@Nonnull Element element) {
        signature = element.getAttribute("signature");
        args = nodes(element, Element.class)
                .filter(e -> "arg".equals(e.getTagName()))
                .map(BeanMember::new)
                .toArray(BeanMember[]::new);
    }

    public String name() {
        final int index = signature.indexOf('(');
        if (index < 0) {
            return signature;
        } else {
            return signature.substring(0, index);
        }
    }

    public String[] argTypes() {
        final int index = signature.indexOf('(');
        if (index < 0) {
            return new String[0];
        } else {
            final String args = signature.substring(index + 1, signature.length() - 1);
            return args.isEmpty() ? new String[0] : args.split(",");
        }
    }

    public void writeTo(@Nonnull Element element) {
        element.setAttribute("signature", signature);
        for (final BeanMember arg : args) {
            arg.writeTo(build(element.getOwnerDocument().createElement("arg"), element::appendChild));
        }
    }

    public boolean matches(Class<?>... argTypes) {
        final String[] types = argTypes();
        if (argTypes.length == types.length) {
            for (int i = 0; i < argTypes.length; i++) {
                if (!types[i].equals(argTypes[i].getName())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final BeanProducer that = (BeanProducer) o;
            return Objects.equals(signature, that.signature) && Arrays.equals(args, that.args);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, args);
    }

    @Override
    public String toString() {
        return String.format("%s%s", signature, Arrays.toString(args));
    }
}
