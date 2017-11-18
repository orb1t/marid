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
import org.marid.types.TypeContext;

import java.io.Closeable;
import java.io.Writer;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.marid.runtime.context.MaridRuntimeUtils.types;

@Tag("normal")
class TypeContextTest {

  private final TypeContext context = new TypeContext(Thread.currentThread().getContextClassLoader());

  @Test
  void test1() {
    final Type from = List2.class;

    final Map<TypeVariable<?>, Type> map = context.resolveVars(from);

    final Set<Class<?>> ec = types(List2.class).filter(c -> c.getTypeParameters().length > 0).collect(toSet());
    final Set<Class<?>> ac = map.keySet().stream().map(v -> (Class<?>) v.getGenericDeclaration()).collect(toSet());

    assertEquals(ec, ac);

    final Set<TypeVariable<?>> expectedVars = ec.stream().flatMap(c -> of(c.getTypeParameters())).collect(toSet());

    assertEquals(expectedVars, map.keySet());
  }

  @Test
  void test2() {
    final Type from = List2.M.class;
    final Class<?> to = List.class;

    final Map<TypeVariable<?>, Type> actual = context.resolveVars(from);

    System.out.println(actual);
  }

  public static class List1<E extends Closeable> extends ArrayList<E> {

    public abstract class L<X extends E> implements List<X> {
    }

    public abstract class M implements List<E> {
    }
  }

  public static class List2 extends List1<Writer> {
  }
}
