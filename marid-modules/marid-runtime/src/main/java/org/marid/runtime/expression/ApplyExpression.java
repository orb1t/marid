package org.marid.runtime.expression;

import javax.annotation.Nonnull;
import java.util.Map;

public interface ApplyExpression extends Expression {
    @Nonnull
    Expression getTarget();

    @Nonnull
    String getMethod();

    @Nonnull
    String getType();

    @Nonnull
    Map<String, Expression> getArgs();
}
