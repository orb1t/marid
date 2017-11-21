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

package org.marid.runtime.types;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.runtime.types.AuxTypeUtils.I1;
import org.marid.runtime.types.AuxTypeUtils.Map1;
import org.marid.types.Types;

import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.reflect.TypeUtils.parameterize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.marid.runtime.types.AuxTypeUtils.p;
import static org.marid.runtime.types.AuxTypeUtils.w;
import static org.marid.types.Classes.classes;

@Tag("normal")
class TypesTest {

  @Test
  void resolveVars1() {
    final Map<TypeVariable<?>, Type> map = Types.resolveVars(AuxTypeUtils.List2.class);

    final Set<Class<?>> ec = classes(AuxTypeUtils.List2.class).filter(c -> c.getTypeParameters().length > 0).collect(toSet());
    final Set<Class<?>> ac = map.keySet().stream().map(v -> (Class<?>) v.getGenericDeclaration()).collect(toSet());
    final Set<TypeVariable<?>> expectedVars = ec.stream().flatMap(c -> of(c.getTypeParameters())).collect(toSet());

    assertEquals(ec, ac);
    assertEquals(expectedVars, map.keySet());
    assertEquals(Set.of(Writer.class), new HashSet<>(map.values()));
  }

  @Test
  void resolveVars2() {
    final Map<TypeVariable<?>, Type> map = Types.resolveVars(AuxTypeUtils.List2.M.class);

    final Set<Class<?>> ec = classes(AuxTypeUtils.List1.M.class).filter(c -> c.getTypeParameters().length > 0).collect(toSet());
    final Set<Class<?>> ac = map.keySet().stream().map(v -> (Class<?>) v.getGenericDeclaration()).collect(toSet());
    final Set<TypeVariable<?>> expectedVars = ec.stream().flatMap(c -> of(c.getTypeParameters())).collect(toSet());

    assertEquals(ec, ac);
    assertEquals(expectedVars, map.keySet());
  }

  @Test
  void resolveVarsStackOverflow() {
    final Map<TypeVariable<?>, Type> map = Types.resolveVars(AuxTypeUtils.Map2.class);

    assertEquals(Set.of(Map1.class.getTypeParameters()), map.keySet());
    assertEquals(Set.of(parameterize(I1.class, I1.class)), new HashSet<>(map.values()));
  }

  private static Stream<Arguments> typesData() {
    return of(
        Arguments.of(p(ArrayList.class, Integer.class), Arrays.asList(
            p(ArrayList.class, Integer.class),
            p(AbstractList.class, Integer.class),
            p(AbstractCollection.class, Integer.class),
            Object.class,
            p(List.class, Integer.class),
            p(Collection.class, Integer.class),
            p(Iterable.class, Integer.class),
            RandomAccess.class,
            Cloneable.class,
            Serializable.class
        )),
        Arguments.of(p(HashMap.class, Integer.class, w(Long.class)), Arrays.asList(
            p(HashMap.class, Integer.class, w(Long.class)),
            p(AbstractMap.class, Integer.class, w(Long.class)),
            Object.class,
            p(Map.class, Integer.class, w(Long.class)),
            Cloneable.class,
            Serializable.class
        ))
    );
  }

  @ParameterizedTest
  @MethodSource("typesData")
  void types(Type type, List<Type> expected) {
    final List<Type> actual = Types.types(type).collect(toList());
    assertEquals(expected, actual);
  }
}