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

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuavaResolverTest {

    @Test
    void testResolveWithOwner() throws Exception {
        class X extends ArrayList<Integer> {
        }

        final Method m1 = List.class.getMethod("add", Object.class);
        final Type argType = m1.getGenericParameterTypes()[0];
        final TypeToken<?> ownerToken = TypeToken.of(X.class);
        final TypeToken<?> resolvedToken = ownerToken.resolveType(argType);
        final Type resolvedType = resolvedToken.getType();
        assertEquals(Integer.class, resolvedType);
    }

    @Test
    void testResolver1() throws Exception {
        final Method m1 = List.class.getMethod("add", Object.class);
        final Type listType = TypeToken.of(List.class).getSupertype(List.class).getType();
        final Type type = new TypeResolver()
                .where(m1.getGenericParameterTypes()[0], Integer.class)
                .resolveType(listType);
        assertEquals(new TypeToken<List<Integer>>() {}.getType(), type);
    }
}
