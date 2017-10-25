/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.meta;

import org.junit.jupiter.api.Test;
import org.marid.beans.IdeBean;
import org.marid.expression.mutable.ClassExpr;
import org.marid.expression.mutable.NullExpr;
import org.marid.expression.mutable.RefExpr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.marid.meta.GuavaTypeContext.WILDCARD;

class BeanTypeTest {

    private final Properties properties = new Properties();
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Test
    void simple() {
        final IdeBean root = new IdeBean()
                .add("b1", new ClassExpr("java.lang.Integer"))
                .add("b2", new NullExpr())
                .add("b3", new RefExpr("b1"));

        assertAll("Type resolving mismatch", () -> {
            final Type type = root.children.get(0).getType(classLoader, properties);
            assertTrue(type instanceof ParameterizedType, "b1");
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            assertEquals(Integer.class, parameterizedType.getActualTypeArguments()[0], "b1");
        }, () -> {
            final Type type = root.children.get(1).getType(classLoader, properties);
            assertTrue(type instanceof WildcardType, "b2");
            assertSame(WILDCARD, type, "b2");
        }, () -> {
            final Type type = root.children.get(2).getType(classLoader, properties);
            assertTrue(type instanceof ParameterizedType, "b3");
        });
    }
}
