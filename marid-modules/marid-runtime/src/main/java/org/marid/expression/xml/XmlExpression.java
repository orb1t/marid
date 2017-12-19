/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.expression.xml;

import org.jetbrains.annotations.NotNull;
import org.marid.expression.generic.*;
import org.marid.runtime.MaridFactory;
import org.marid.xml.Tagged;
import org.marid.xml.XmlWritable;
import org.w3c.dom.Element;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static org.marid.io.Xmls.*;

public interface XmlExpression {

  @NotNull
  static <E extends Expression> E target(@NotNull Element element,
                                         @NotNull Function<Element, E> exprFunc,
                                         @NotNull Function<String, E> classExprFunc,
                                         @NotNull Function<String, E> refExprFunc) {
    return element("target", element)
        .map(exprFunc)
        .orElseGet(() -> attribute(element, "class")
            .map(classExprFunc)
            .orElseGet(() -> attribute(element, "ref")
                .map(refExprFunc)
                .orElseGet(() -> classExprFunc.apply(MaridFactory.class.getName()))));
  }

  static <E extends Expression & Tagged & XmlWritable> void target(@NotNull Element element, @NotNull E target) {
    if (target instanceof ClassExpression) {
      final ClassExpression expression = (ClassExpression) target;
      if (!MaridFactory.class.getName().equals(expression.getClassName())) {
        element.setAttribute("class", expression.getClassName());
      }
    } else if (target instanceof RefExpression) {
      element.setAttribute("ref", ((RefExpression) target).getReference());
    } else {
      create(element, "target", t -> create(t, target.getTag(), target::writeTo));
    }
  }

  @NotNull
  static <E extends Expression, L extends List<E>> L args(@NotNull Element element,
                                                          @NotNull Function<Element, E> exprFunc,
                                                          @NotNull Function<String, E> stringFunc,
                                                          @NotNull Collector<E, ?, L> collector) {
    return attribute(element, "arg")
        .map(c -> Stream.of(c).map(stringFunc).collect(collector))
        .orElseGet(() -> elements("args", element).map(exprFunc).collect(collector));
  }

  static <E extends Expression & Tagged & XmlWritable> void args(@NotNull Element element,
                                                                 @NotNull List<? extends E> args) {
    switch (args.size()) {
      case 0:
        break;
      case 1:
        if (args.get(0) instanceof StringExpression) {
          final StringExpression expression = (StringExpression) args.get(0);
          final String value = expression.getValue();
          if (value.length() < 256) {
            element.setAttribute("arg", value);
            break;
          }
        }
      default:
        create(element, "args", is -> args.forEach(i -> create(is, i.getTag(), i::writeTo)));
        break;
    }
  }

  @NotNull
  static <E extends Expression, M extends MappedExpression, L extends List<M>>
  L mappedArgs(@NotNull Element element,
               @NotNull Function<Element, E> exprFunc,
               @NotNull BiFunction<Integer, E, M> mappedExprFunc,
               @NotNull Collector<M, ?, L> collector) {
    return elements("args", element)
        .map(e -> {
          final int index = attribute(e, "index").map(Integer::valueOf).orElse(-1);
          final E expr = exprFunc.apply(e);
          return mappedExprFunc.apply(index, expr);
        })
        .collect(collector);
  }

  static <M extends MappedExpression & Tagged & XmlWritable> void mappedArgs(@NotNull Element element,
                                                                             @NotNull List<? extends M> args) {
    create(element, "args", is -> args.forEach(i -> create(is, i.getTag(), i::writeTo)));
  }

  @NotNull
  static String method(@NotNull Element element) {
    return attribute(element, "method").orElse("int32");
  }

  static void method(@NotNull Element element, @NotNull String method) {
    if (!"int32".equals(method)) {
      element.setAttribute("method", method);
    }
  }

  @NotNull
  static String field(@NotNull Element element) {
    return attribute(element, "field").orElse("value");
  }

  static void field(@NotNull Element element, @NotNull String field) {
    if (!"value".equals(field)) {
      element.setAttribute("field", field);
    }
  }

  @NotNull
  static <E extends Expression> E value(@NotNull Element element,
                                        @NotNull Function<Element, E> exprFunc,
                                        @NotNull Supplier<E> nullExprFunc) {
    return element("value", element).map(exprFunc).orElseGet(nullExprFunc);
  }

  static <E extends Expression & Tagged & XmlWritable> void value(@NotNull Element element,
                                                                  @NotNull E expr) {
    if (expr instanceof NullExpression && void.class.getName().equals(((NullExpression) expr).getType())) {
      return;
    }
    create(element, "value", v -> create(v, expr.getTag(), expr::writeTo));
  }

  @NotNull
  static String ref(@NotNull Element element) {
    return attribute(element, "ref").orElseThrow(() -> new NullPointerException("ref is absent"));
  }

  static void ref(@NotNull Element element, @NotNull String ref) {
    element.setAttribute("ref", ref);
  }

  @NotNull
  static String type(@NotNull Element element) {
    return attribute(element, "type").orElseGet(void.class::getName);
  }

  static void type(@NotNull Element element, @NotNull String type) {
    if (!void.class.getName().equals(type)) {
      element.setAttribute("type", type);
    }
  }

  @NotNull
  static String className(@NotNull Element element) {
    return attribute(element, "class").orElseGet(MaridFactory.class::getName);
  }

  static void className(@NotNull Element element, @NotNull String className) {
    if (!MaridFactory.class.getName().equals(className)) {
      element.setAttribute("class", className);
    }
  }

  @NotNull
  static String string(@NotNull Element element) {
    return content(element).orElse("");
  }

  static void string(@NotNull Element element, @NotNull String value) {
    if (!"".equals(value)) {
      element.setTextContent(value);
    }
  }

  @NotNull
  static <E extends Expression, L extends List<E>> L arrayElems(@NotNull Element element,
                                                                @NotNull Function<Element, E> exprFunc,
                                                                @NotNull Collector<E, ?, L> collector) {
    return elements("elements", element).map(exprFunc).collect(collector);
  }

  static <E extends Expression & Tagged & XmlWritable> void arrayElems(@NotNull Element element,
                                                                       @NotNull List<? extends E> elems) {
    if (!elems.isEmpty()) {
      create(element, "elements", is -> elems.forEach(i -> create(is, i.getTag(), i::writeTo)));
    }
  }

  @NotNull
  static <E extends Expression, L extends List<E>> L initializers(@NotNull Element element,
                                                                  @NotNull Function<Element, E> exprFunc,
                                                                  @NotNull Collector<E, ?, L> collector) {
    return elements("initializers", element).map(exprFunc).collect(collector);
  }

  static <E extends Expression & Tagged & XmlWritable> void initializers(@NotNull Element element,
                                                                         @NotNull List<? extends E> list) {
    if (!list.isEmpty()) {
      create(element, "initializers", is -> list.forEach(i -> create(is, i.getTag(), i::writeTo)));
    }
  }
}
