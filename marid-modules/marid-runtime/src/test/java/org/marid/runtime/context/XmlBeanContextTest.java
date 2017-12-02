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

package org.marid.runtime.context;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.beans.RuntimeBean;
import org.marid.io.Xmls;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("normal")
class XmlBeanContextTest {

  private static Properties properties;
  private static ClassLoader classLoader;
  private static BeanConfiguration beanConfiguration;
  private static BeanContext context;

  @BeforeAll
  static void init() throws IOException {
    properties = PropertiesLoaderUtils.loadAllProperties("classpath:beans.properties");
    classLoader = Thread.currentThread().getContextClassLoader();
    beanConfiguration = new BeanConfiguration(classLoader, properties);

    try (final Reader reader = new InputStreamReader(classLoader.getResourceAsStream("beans1.xml"), UTF_8)) {
      context = new BeanContext(beanConfiguration, Xmls.read(reader, e -> new RuntimeBean(null, e)));
    }
  }

  @AfterAll
  static void destroy() {
    if (context != null) {
      context.close();
    }
  }

  private static Stream<Arguments> data() {
    return Stream.of(
        () -> new Object[] {"b1", "str"},
        () -> new Object[] {"b2", BigInteger.ONE},
        () -> new Object[] {"b3", singletonList(BigInteger.ONE)},
        () -> new Object[] {"b4", singletonList(BigInteger.ONE)}
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void testBean(String bean, Object expected) {
    final BeanContext b = context.children()
        .filter(c -> c.getBean().getName().equals(bean))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException(bean));
    assertEquals(expected, b.getInstance());
  }
}
