/*-
 * #%L
 * marid-ide
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

package org.marid.expression.mutable;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.beans.BeanTypeContext;
import org.marid.expression.mutable.testclasses.MyComplexBean;
import org.marid.expression.mutable.testclasses.MyList;
import org.marid.idelib.beans.BeanUtils;
import org.marid.idelib.beans.IdeBean;
import org.marid.idelib.beans.IdeBeanContext;
import org.marid.io.Xmls;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("normal")
class ExpressionTypeTest {

  private static ClassLoader classLoader;
  private static IdeBean root;

  @BeforeAll
  static void init() throws IOException {
    classLoader = Thread.currentThread().getContextClassLoader();
    try (final Reader reader = new InputStreamReader(classLoader.getResourceAsStream("tbeans1.xml"), UTF_8)) {
      root = Xmls.read(reader, e -> new IdeBean(null, e));
    }
  }

  private static Stream<Arguments> testData() {
    return Stream.of(
        () -> new Object[]{"b01", String.class},
        () -> new Object[]{"b02", BigInteger.class},
        () -> new Object[]{"b03", new TypeToken<ArrayList<Integer>>() {}.getType()},
        () -> new Object[]{"b04", int.class},
        () -> new Object[]{"b05", new TypeToken<List<Long>>() {}.getType()},
        () -> new Object[]{"b06", new TypeToken<List<List<Long>>>() {}.getType()},
        () -> new Object[]{"b07", new TypeToken<List<Integer>>() {}.getType()},
        () -> new Object[]{"b08", new TypeToken<ArrayList<Number>>() {}.getType()},
        () -> new Object[]{"b09", new TypeToken<List<List<Integer>>>() {}.getType()},
        () -> new Object[]{"b10", new TypeToken<List<Long>[]>() {}.getType()},
        () -> new Object[]{"b11", int[].class},
        () -> new Object[]{"b12", new TypeToken<ArrayList<Number[]>>() {}.getType()},
        () -> new Object[]{"b13", new TypeToken<MyList<AutoCloseable>>() {}.getType()},
        () -> new Object[]{"b14", new TypeToken<MyComplexBean<Integer, Integer>>() {}.getType()},
        () -> new Object[]{"b15", new TypeToken<List<Number>>() {}.getType()},
        () -> new Object[]{"b16", int.class}
    );
  }

  @ParameterizedTest
  @MethodSource("testData")
  void testBean(String beanName, Type expectedType) {
    final IdeBean bean = BeanUtils.find(root, beanName);
    final BeanTypeContext context = new IdeBeanContext(bean, classLoader);
    final Type type = bean.getFactory().getType(null, context);
    assertEquals(expectedType, type);
  }
}
