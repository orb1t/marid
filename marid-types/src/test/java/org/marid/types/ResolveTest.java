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

import org.junit.jupiter.api.Test;
import org.marid.types.AuxTypeUtils.C0;
import org.marid.types.AuxTypeUtils.C1;
import org.marid.types.AuxTypeUtils.C2;
import org.marid.types.AuxTypeUtils.MyList;
import org.marid.types.util.MappedVars;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.marid.types.AuxTypeUtils.p;
import static org.marid.types.Types.evaluate;
import static org.marid.types.Types.getType;

class ResolveTest {

  @Test
  void resolveMap() throws Exception {
    final Method method = Map.class.getMethod("put", Object.class, Object.class);
    final Type[] types = method.getGenericParameterTypes();
    final Type resolved = evaluate(e -> {
      e.bind(types[0], Long.class);
      e.bind(types[1], float.class);
    }, getType(Map.class));
    assertEquals(p(Map.class, Long.class, Float.class), resolved);
  }

  @Test
  void resolveVars() {
    final Type type = p(C2.class, Integer.class);
    final MappedVars map = Types.resolveVars(type);

    final TypeVariable<?> c0Var = C0.class.getTypeParameters()[0];
    final TypeVariable<?> c1Var = C1.class.getTypeParameters()[0];
    final TypeVariable<?> c2Var = C2.class.getTypeParameters()[0];

    assertEquals(Integer.class, map.get(c2Var));
    assertEquals(p(List.class, Integer.class), map.get(c1Var));
    assertEquals(p(List.class, p(List.class, Integer.class)), map.get(c0Var));
  }

  @Test
  void resolveLinkedHashMap() throws Exception {
    final Method method = Map.class.getMethod("put", Object.class, Object.class);
    final Type[] types = method.getGenericParameterTypes();
    final Type resolved = evaluate(e -> {
      e.bind(types[0], Long.class);
      e.bind(types[1], float.class);
    }, getType(LinkedHashMap.class));
    assertEquals(p(LinkedHashMap.class, Long.class, Float.class), resolved);
  }

  @Test
  void resolveMyListList() throws Exception {
    final Method method = ArrayList.class.getMethod("add", Object.class);
    final Type[] types = method.getGenericParameterTypes();
    final Type resolved = evaluate(e -> e.bind(types[0], p(List.class, Integer.class)), getType(MyList.class));
    assertEquals(p(MyList.class, Integer.class), resolved);
  }

  @Test
  void resolveMyListArrayList() throws Exception {
    final Method method = ArrayList.class.getMethod("add", Object.class);
    final Type[] types = method.getGenericParameterTypes();
    final Type resolved = evaluate(e -> e.bind(types[0], p(ArrayList.class, Integer.class)), getType(MyList.class));
    assertEquals(p(MyList.class, Integer.class), resolved);
  }
}
