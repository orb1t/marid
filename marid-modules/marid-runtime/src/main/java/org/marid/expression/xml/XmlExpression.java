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

import org.marid.XmlWritable;
import org.marid.expression.generic.*;
import org.marid.runtime.MaridFactory;
import org.marid.xml.Tagged;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static org.marid.io.Xmls.*;

public interface XmlExpression {

  @Nonnull
  static <E extends Expression> E target(@Nonnull Element element,
                                         @Nonnull Function<Element, E> exprFunc,
                                         @Nonnull Function<String, E> classExprFunc,
                                         @Nonnull Function<String, E> refExprFunc) {
    return element("target", element)
        .map(exprFunc)
        .orElseGet(() -> attribute(element, "class")
            .map(classExprFunc)
            .orElseGet(() -> attribute(element, "ref")
                .map(refExprFunc)
                .orElseGet(() -> classExprFunc.apply(MaridFactory.class.getName()))));
  }

  static <E extends Expression & Tagged & XmlWritable> void target(@Nonnull Element element, @Nonnull E target) {
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

  @Nonnull
  static <E extends Expression, L extends List<E>> L args(@Nonnull Element element,
                                                          @Nonnull Function<Element, E> exprFunc,
                                                          @Nonnull Function<String, E> stringFunc,
                                                          @Nonnull Collector<E, ?, L> collector) {
    return attribute(element, "arg")
        .map(c -> Stream.of(c).map(stringFunc).collect(collector))
        .orElseGet(() -> elements("args", element).map(exprFunc).collect(collector));
  }

  static <E extends Expression & Tagged & XmlWritable> void args(@Nonnull Element element,
                                                                 @Nonnull List<? extends E> args) {
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

  @Nonnull
  static String method(@Nonnull Element element) {
    return attribute(element, "method").orElse("int32");
  }

  static void method(@Nonnull Element element, @Nonnull String method) {
    if (!"int32".equals(method)) {
      element.setAttribute("method", method);
    }
  }

  @Nonnull
  static String field(@Nonnull Element element) {
    return attribute(element, "field").orElse("value");
  }

  static void field(@Nonnull Element element, @Nonnull String field) {
    if (!"value".equals(field)) {
      element.setAttribute("field", field);
    }
  }

  @Nonnull
  static <E extends Expression> E value(@Nonnull Element element,
                                        @Nonnull Function<Element, E> exprFunc,
                                        @Nonnull Supplier<E> nullExprFunc) {
    return element("value", element).map(exprFunc).orElseGet(nullExprFunc);
  }

  static <E extends Expression & Tagged & XmlWritable> void value(@Nonnull Element element,
                                                                  @Nonnull E expr) {
    if (expr instanceof NullExpression && void.class.getName().equals(((NullExpression) expr).getType())) {
      return;
    }
    create(element, "value", v -> create(v, expr.getTag(), expr::writeTo));
  }

  @Nonnull
  static String ref(@Nonnull Element element) {
    return attribute(element, "ref").orElseThrow(() -> new NullPointerException("ref is absent"));
  }

  static void ref(@Nonnull Element element, @Nonnull String ref) {
    element.setAttribute("ref", ref);
  }

  @Nonnull
  static String type(@Nonnull Element element) {
    return attribute(element, "type").orElseGet(void.class::getName);
  }

  static void type(@Nonnull Element element, @Nonnull String type) {
    if (!void.class.getName().equals(type)) {
      element.setAttribute("type", type);
    }
  }

  @Nonnull
  static String className(@Nonnull Element element) {
    return attribute(element, "class").orElseGet(MaridFactory.class::getName);
  }

  static void className(@Nonnull Element element, @Nonnull String className) {
    if (!MaridFactory.class.getName().equals(className)) {
      element.setAttribute("class", className);
    }
  }

  @Nonnull
  static String string(@Nonnull Element element) {
    return content(element).orElse("");
  }

  static void string(@Nonnull Element element, @Nonnull String value) {
    if (!"".equals(value)) {
      element.setTextContent(value);
    }
  }

  @Nonnull
  static <E extends Expression, L extends List<E>> L arrayElems(@Nonnull Element element,
                                                                @Nonnull Function<Element, E> exprFunc,
                                                                @Nonnull Collector<E, ?, L> collector) {
    return elements("elements", element).map(exprFunc).collect(collector);
  }

  static <E extends Expression & Tagged & XmlWritable> void arrayElems(@Nonnull Element element,
                                                                       @Nonnull List<? extends E> elems) {
    if (!elems.isEmpty()) {
      create(element, "elements", is -> elems.forEach(i -> create(is, i.getTag(), i::writeTo)));
    }
  }

  @Nonnull
  static <E extends Expression, L extends List<E>> L initializers(@Nonnull Element element,
                                                                  @Nonnull Function<Element, E> exprFunc,
                                                                  @Nonnull Collector<E, ?, L> collector) {
    return elements("initializers", element).map(exprFunc).collect(collector);
  }

  static <E extends Expression & Tagged & XmlWritable> void initializers(@Nonnull Element element,
                                                                         @Nonnull List<? extends E> list) {
    if (!list.isEmpty()) {
      create(element, "initializers", is -> list.forEach(i -> create(is, i.getTag(), i::writeTo)));
    }
  }
}
