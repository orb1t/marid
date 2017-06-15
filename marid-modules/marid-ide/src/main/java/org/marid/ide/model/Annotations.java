/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        return node.getAnnotationByClass(Lazy.class)
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
        return node.getAnnotationByClass(Scope.class)
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
        return node.getAnnotationByClass(Value.class)
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
        return node.getAnnotationByClass(Qualifier.class)
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
