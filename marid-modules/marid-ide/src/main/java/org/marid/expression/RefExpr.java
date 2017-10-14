package org.marid.expression;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.runtime.expression.RefExpression;

import javax.annotation.Nonnull;

public class RefExpr extends AbstractExpression implements RefExpression {

    public final StringProperty ref = new SimpleStringProperty();

    public RefExpr() {
        this("");
    }

    public RefExpr(@Nonnull String ref) {
        this.ref.set(ref);
    }

    @Nonnull
    @Override
    public String getReference() {
        return ref.get();
    }

    @Override
    public void setReference(@Nonnull String reference) {
        this.ref.set(reference);
    }
}
