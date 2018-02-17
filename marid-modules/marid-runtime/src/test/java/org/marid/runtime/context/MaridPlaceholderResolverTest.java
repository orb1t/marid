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

import org.marid.runtime.common.MaridPlaceholderResolver;
import org.marid.runtime.exception.CircularPlaceholderException;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.marid.test.TestGroups.NORMAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridPlaceholderResolverTest {

  @Test(groups = {NORMAL})
  void circular1() {
    assertThrows(CircularPlaceholderException.class, () -> {
      final Properties properties = new Properties();
      properties.setProperty("x1", "2");
      properties.setProperty("x2", "${x3}");
      properties.setProperty("x3", "${x2}");
      final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
      resolver.resolvePlaceholders("abc ${x2}");
    });
  }

  @Test(groups = {NORMAL})
  void circular2() {
    assertThrows(CircularPlaceholderException.class, () -> {
      final Properties properties = new Properties();
      properties.setProperty("x1", "2");
      properties.setProperty("x2", "${x2}");
      final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
      resolver.resolvePlaceholders("abc ${x2}");
    });
  }

  @Test(groups = {NORMAL})
  void defValue() {
    final Properties properties = new Properties();
    properties.setProperty("x1", "2");
    final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
    assertEquals(resolver.resolvePlaceholders("abc ${x2:zz} ${x1}"), "abc zz 2");
  }

  @Test(groups = {NORMAL})
  void unterminated() {
    final Properties properties = new Properties();
    properties.setProperty("x1", "2");
    final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
    assertEquals(resolver.resolvePlaceholders("abc ${x2:zz} ${x1"), "abc zz ${x1");
    assertEquals(resolver.resolvePlaceholders("abc ${x2} ${x1"), "abc  ${x1");
  }
}
