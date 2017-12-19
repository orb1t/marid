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

package org.marid.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.expression.runtime.StringExpr;
import org.marid.runtime.context.BeanConfiguration;
import org.marid.runtime.context.BeanContext;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class BeanTypeTest {

  private static final BeanContext CONTEXT = new BeanContext(
      new BeanConfiguration(Thread.currentThread().getContextClassLoader(), new Properties()),
      new RuntimeBean(
          new RuntimeBean("bean1", new StringExpr(""))
      )
  );

  private static Stream<Arguments> typeData() {
    return Stream.of(
        Arguments.of("bean1", "java.lang.String")
    );
  }

  @ParameterizedTest
  @MethodSource({"typeData"})
  void type(String beanPath, String expectedType) {
    final MaridBean bean = Pattern.compile("/").splitAsStream(beanPath).reduce(
        CONTEXT.getBean(),
        (b, p) -> b.getChildren().stream()
            .filter(e -> e.getName().equals(p))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(p)),
        (b1, b2) -> b2);
    final TestBeanTypeContext context = new TestBeanTypeContext(bean);
    final Type type = bean.getFactory().getType(null, context);
    final String typeName = type.getTypeName();
    Assertions.assertEquals(typeName, expectedType);
  }
}
