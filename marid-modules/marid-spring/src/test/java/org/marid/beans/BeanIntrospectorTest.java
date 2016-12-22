/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.beans;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.beans.testbeans.Bean1;
import org.marid.beans.testbeans.Bean2;
import org.marid.beans.testbeans.Bean2Provider;
import org.marid.test.NormalTests;

import java.lang.reflect.Method;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.marid.beans.BeanIntrospector.classInfo;
import static org.springframework.core.ResolvableType.forClass;
import static org.springframework.core.ResolvableType.forMethodReturnType;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Category({NormalTests.class})
public class BeanIntrospectorTest {

    @Test
    public void bean1Test() {
        final ClassInfo classInfo = classInfo(Bean1.class.getClassLoader(), forClass(Bean1.class));
        assertEquals(2, classInfo.constructorInfos.length);
        assertEquals("init", classInfo.constructorInfos[0].name);
        assertEquals("d", classInfo.description);
        assertEquals("x", classInfo.title);
        assertEquals(BigInteger.class, classInfo.editor);
        assertEquals("constructor 1", classInfo.constructorInfos[0].description);
    }

    @Test
    public void bean2Test() throws Exception {
        final Method providerMethod = Bean2Provider.class.getMethod("bean2");
        final ClassInfo classInfo = classInfo(Bean2.class.getClassLoader(), forMethodReturnType(providerMethod));
        assertEquals(1, classInfo.constructorInfos.length);
        assertEquals("argument", classInfo.constructorInfos[0].parameters[0].title);
    }
}
