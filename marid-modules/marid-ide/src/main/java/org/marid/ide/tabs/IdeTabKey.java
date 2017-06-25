package org.marid.ide.tabs;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class IdeTabKey {

    public final ObservableValue<String> textBinding;
    public final Supplier<Node> graphicBinding;

    public IdeTabKey(ObservableValue<String> textBinding, Supplier<Node> graphicBinding) {
        this.textBinding = textBinding;
        this.graphicBinding = graphicBinding;
    }

    @Override
    public int hashCode() {
        return textBinding.getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        final IdeTabKey that = (IdeTabKey) obj;
        return this.textBinding.getValue().equals(that.textBinding.getValue());
    }
}
