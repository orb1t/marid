/*-
 * #%L
 * marid-types
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

import org.marid.types.AuxTypeUtils.C0;
import org.marid.types.AuxTypeUtils.C1;
import org.marid.types.AuxTypeUtils.C2;
import org.marid.types.AuxTypeUtils.MyList;
import org.marid.types.util.MappedVars;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

import static org.marid.test.TestGroups.NORMAL;
import static org.marid.types.AuxTypeUtils.p;
import static org.marid.types.Types.evaluate;
import static org.marid.types.Types.getType;
import static org.testng.Assert.assertEquals;

public class ResolveTest {

  @Test(groups = {NORMAL})
  public void resolveMap() throws Exception {
    final Method method = Map.class.getMethod("put", Object.class, Object.class);
    final Type[] types = method.getGenericParameterTypes();
    final Type resolved = evaluate(e -> {
      e.bind(types[0], Long.class);
      e.bind(types[1], float.class);
    }, getType(Map.class));
    assertEquals(resolved, p(Map.class, Long.class, Float.class));
  }

  @Test(groups = {NORMAL})
  public void resolveVars() {
    final Type type = p(C2.class, Integer.class);
    final MappedVars map = Types.resolveVars(type);

    final TypeVariable<?> c0Var = C0.class.getTypeParameters()[0];
    final TypeVariable<?> c1Var = C1.class.getTypeParameters()[0];
    final TypeVariable<?> c2Var = C2.class.getTypeParameters()[0];

    assertEquals(map.get(c2Var), Integer.class);
    assertEquals(map.get(c1Var), p(List.class, Integer.class));
    assertEquals(map.get(c0Var), p(List.class, p(List.class, Integer.class)));
  }

  @DataProvider
  public static Object[][] resolveLinkedHashMapData() throws Exception {
    return new Object[][] {
        {Map.class.getMethod("put", Object.class, Object.class).getGenericParameterTypes()},
        {AbstractMap.class.getMethod("put", Object.class, Object.class).getGenericParameterTypes()},
        {HashMap.class.getMethod("put", Object.class, Object.class).getGenericParameterTypes()}
    };
  }

  @Test(groups = {NORMAL}, dataProvider = "resolveLinkedHashMapData")
  public void resolveLinkedHashMap(Type[] types) {
    final Type resolved = evaluate(e -> {
      e.bind(types[0], Long.class);
      e.bind(types[1], float.class);
    }, getType(LinkedHashMap.class));
    assertEquals(p(LinkedHashMap.class, Long.class, Float.class), resolved);
  }

  @DataProvider
  public static Object[][] resolveMyListData() throws Exception {
    return new Object[][]{
        {List.class.getMethod("add", Object.class).getGenericParameterTypes(), p(List.class, Integer.class)},
        {ArrayList.class.getMethod("add", Object.class).getGenericParameterTypes(), p(ArrayList.class, Integer.class)},
        {Collection.class.getMethod("add", Object.class).getGenericParameterTypes(), p(AbstractList.class, Integer.class)}
    };
  }

  @Test(groups = {NORMAL}, dataProvider = "resolveMyListData")
  public void resolveMyList(Type[] types, Type bindType) {
    final Type resolved = evaluate(e -> e.bind(types[0], bindType), getType(MyList.class));
    assertEquals(p(MyList.class, Integer.class), resolved);
  }
}
