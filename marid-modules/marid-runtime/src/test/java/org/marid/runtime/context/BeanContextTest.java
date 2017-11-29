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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.beans.MaridBean;
import org.marid.beans.RuntimeBean;
import org.marid.expression.runtime.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.List.of;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("normal")
class BeanContextTest {

  private static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();
  private static final Properties PROPERTIES = new Properties();

  @Test
  void testOneBean() {
    final BeanConfiguration configuration = new BeanConfiguration(CLASS_LOADER, PROPERTIES);
    final RuntimeBean root = new RuntimeBean(
        new RuntimeBean("bean1", new GetExpr(new ClassExpr(TimeUnit.class.getName()), "SECONDS"))
    );
    try (final BeanContext context = new BeanContext(configuration, root)) {
      final Object seconds = context.findBean("bean1");
      assertEquals(TimeUnit.SECONDS, seconds);
    }
  }

  @Test
  void testTwoBeans() {
    final BeanConfiguration configuration = new BeanConfiguration(CLASS_LOADER, PROPERTIES);
    final RuntimeBean root = new RuntimeBean(
        new RuntimeBean("bean1", new CallExpr(new ClassExpr(TimeUnit.class.getName()), "valueOf", new RefExpr("bean2"))),
        new RuntimeBean("bean2", new StringExpr("SECONDS"))
    );
    try (final BeanContext context = new BeanContext(configuration, root)) {
      final Object seconds = context.findBean("bean1");
      assertEquals(TimeUnit.SECONDS, seconds);
    }
  }

  private static Stream<Arguments> matchingCandidatesArguments() {
    final NullExpr n = new NullExpr();
    final RuntimeBean root = new RuntimeBean(
        new RuntimeBean("b1", n,
            new RuntimeBean("b11", n, new RuntimeBean("b111", n), new RuntimeBean("b112", n)),
            new RuntimeBean("b12", n)
        ),
        new RuntimeBean("b2", n,
            new RuntimeBean("b21", n,
                new RuntimeBean("b211", n),
                new RuntimeBean("b212", n),
                new RuntimeBean("b213", n, new RuntimeBean("b2131", n), new RuntimeBean("b2132", n))
            ),
            new RuntimeBean("b22", n),
            new RuntimeBean("b23", n)
        )
    );
    final Map<String, List<String>> map = Map.ofEntries(
        entry("b1", of("b2")),
        entry("b11", of("b12", "b1", "b2")),
        entry("b111", of("b112", "b11", "b12", "b1", "b2")),
        entry("b112", of("b111", "b11", "b12", "b1", "b2")),
        entry("b12", of("b11", "b1", "b2")),
        entry("b2", of("b1")),
        entry("b21", of("b22", "b23", "b2", "b1")),
        entry("b211", of("b212", "b213", "b21", "b22", "b23", "b2", "b1")),
        entry("b212", of("b211", "b213", "b21", "b22", "b23", "b2", "b1")),
        entry("b213", of("b211", "b212", "b21", "b22", "b23", "b2", "b1")),
        entry("b2131", of("b2132", "b213", "b211", "b212", "b21", "b22", "b23", "b2", "b1")),
        entry("b2132", of("b2131", "b213", "b211", "b212", "b21", "b22", "b23", "b2", "b1")),
        entry("b22", of("b21", "b23", "b2", "b1")),
        entry("b23", of("b21", "b22", "b2", "b1"))
    );

    return root.descendants().map(b -> () -> new Object[]{b, map.get(b.getName())});
  }

  @ParameterizedTest
  @MethodSource("matchingCandidatesArguments")
  void testMatchingCandidates(MaridBean bean, List<String> expected) {
    final List<String> actual = bean.matchingCandidates().map(MaridBean::getName).collect(toList());

    assertEquals(expected, actual);
  }
}
