/*-
 * #%L
 * marid-types
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

package org.marid.types;

import org.marid.test.TestGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

public class ClassesTest {

  @DataProvider
  public static Object[][] classesTestData() {
    return new Object[][]{
        {
            ArrayList.class,
            asList(
                ArrayList.class,
                AbstractList.class,
                AbstractCollection.class,
                Object.class,
                List.class,
                Collection.class,
                Iterable.class,
                RandomAccess.class,
                Cloneable.class,
                Serializable.class
            )}
    };
  }

  @Test(groups = {TestGroups.NORMAL}, dataProvider = "classesTestData")
  public void testClasses(Class<?> target, List<Class<?>> expected) {
    final List<Class<?>> actual = Classes.classes(target).collect(toList());

    assertEquals(actual, expected);
  }
}
