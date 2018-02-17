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

import org.marid.types.invokable.Invokable;
import org.marid.types.invokable.InvokableConstructor;
import org.marid.types.invokable.InvokableMethod;
import org.springframework.ui.ModelMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_INT_ARRAY;
import static org.marid.test.TestGroups.SLOW;
import static org.marid.types.AuxTypeUtils.p;
import static org.testng.Assert.assertEquals;

public class ApplierTest {

  @DataProvider
  private static Object[][] applyTypeData() throws ReflectiveOperationException {
    return new Object[][] {
        {
            Runnable.class,
            new InvokableMethod(String.class.getMethod("length")),
            Runnable.class, String.class, EMPTY_INT_ARRAY, new Type[0]
        },
        {
            p(Callable.class, Integer.class),
            new InvokableMethod(String.class.getMethod("length")),
            Callable.class, String.class, EMPTY_INT_ARRAY, new Type[0]
        },
        {
            p(Callable.class, p(ArrayList.class, Object.class)),
            new InvokableConstructor(ArrayList.class.getConstructor()),
            Callable.class, ArrayList.class, EMPTY_INT_ARRAY, new Type[0]
        },
        {
            p(Callable.class, p(ArrayList.class, Integer.class)),
            new InvokableConstructor(ArrayList.class.getConstructor(Collection.class)),
            Callable.class, ArrayList.class, EMPTY_INT_ARRAY, new Type[] {p(Collection.class, Integer.class)}
        },
        {
            p(Consumer.class, Integer.class),
            new InvokableMethod(ArrayList.class.getMethod("add", Object.class)),
            Consumer.class, p(ArrayList.class, Integer.class), new int[] {0}, new Type[] {Object.class}
        },
        {
            p(Consumer.class, p(Collection.class, Integer.class)),
            new InvokableMethod(List.class.getMethod("add", int.class, Object.class)),
            Consumer.class, p(List.class, p(Collection.class, Integer.class)), new int[] {1}, new Type[] {int.class, Object.class}
        },
        {
            p(Consumer.class, String.class),
            new InvokableMethod(ModelMap.class.getMethod("put", Object.class, Object.class)),
            Consumer.class, ModelMap.class, new int[] {0}, new Type[] {Object.class, Object.class}
        },
        {
            p(BiConsumer.class, String.class, Integer.class),
            new InvokableMethod(LinkedHashMap.class.getMethod("put", Object.class, Object.class)),
            BiConsumer.class, p(LinkedHashMap.class, String.class, Integer.class), new int[] {0, 1}, new Type[0]
        },
        {
            p(BiConsumer.class, String.class, Integer.class),
            new InvokableMethod(Map.class.getMethod("put", Object.class, Object.class)),
            BiConsumer.class, p(LinkedHashMap.class, String.class, Integer.class), new int[] {0, 1}, new Type[0]
        }
    };
  }

  @Test(groups = {SLOW}, dataProvider = "applyTypeData")
  public void testType(Type expected, Invokable invokable, Class<?> type, Type target, int[] indices, Type[] args) {
    final Method sam = Classes.getSam(type).orElseThrow(IllegalArgumentException::new);
    final Type actual = invokable.type(target, type, sam, indices, args);
    assertEquals(actual, expected);
  }
}
