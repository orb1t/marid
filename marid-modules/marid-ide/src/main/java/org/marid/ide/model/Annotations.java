package org.marid.ide.model;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Annotations {

    static Map<String, Expression> getMembers(AnnotationExpr expr) {
        if (expr instanceof MarkerAnnotationExpr) {
            return Collections.emptyMap();
        } else if (expr instanceof SingleMemberAnnotationExpr) {
            return Collections.singletonMap("value", ((SingleMemberAnnotationExpr) expr).getMemberValue());
        } else {
            final NormalAnnotationExpr annotationExpr = (NormalAnnotationExpr) expr;
            final ImmutableMap.Builder<String, Expression> builder = ImmutableMap.builder();
            annotationExpr.getPairs().forEach(p -> builder.put(p.getNameAsString(), p.getValue()));
            return builder.build();
        }
    }

    static boolean isLazy(NodeWithAnnotations<?> node) {
        return node.getAnnotationByName(Lazy.class.getName())
                .map(a -> {
                    final Map<String, Expression> map = getMembers(a);
                    if (map.isEmpty()) {
                        return true;
                    } else {
                        final Expression e = map.get("value");
                        return e instanceof BooleanLiteralExpr && ((BooleanLiteralExpr) e).getValue();
                    }
                })
                .orElse(false);
    }

    static boolean isPrototype(NodeWithAnnotations<?> node) {
        return node.getAnnotationByName(Scope.class.getName())
                .map(a -> {
                    final Map<String, Expression> map = getMembers(a);
                    if (map.isEmpty()) {
                        return false;
                    } else {
                        final Expression e = map.get("value");
                        return e instanceof StringLiteralExpr && "prototype".equals(((StringLiteralExpr) e).getValue());
                    }
                })
                .orElse(false);
    }

    static String value(NodeWithAnnotations<?> node) {
        return node.getAnnotationByName(Value.class.getName())
                .flatMap(a -> {
                    final Map<String, Expression> map = getMembers(a);
                    if (map.isEmpty()) {
                        return Optional.empty();
                    } else {
                        final Expression e = map.get("value");
                        return e instanceof StringLiteralExpr
                                ? Optional.ofNullable(((StringLiteralExpr) e).getValue())
                                : Optional.empty();
                    }
                })
                .orElse(null);
    }

    static String qualifier(NodeWithAnnotations<?> node) {
        return node.getAnnotationByName(Qualifier.class.getName())
                .flatMap(a -> {
                    final Map<String, Expression> map = getMembers(a);
                    if (map.isEmpty()) {
                        return Optional.empty();
                    } else {
                        final Expression e = map.get("value");
                        return e instanceof StringLiteralExpr
                                ? Optional.ofNullable(((StringLiteralExpr) e).getValue())
                                : Optional.empty();
                    }
                })
                .orElse(null);
    }
}
