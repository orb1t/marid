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

package org.marid.spring.context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.spring.context.custom.Class1;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static com.google.inject.util.Types.subtypeOf;
import static org.junit.Assert.*;
import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.core.ResolvableType.forType;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Category({NormalTests.class})
public class SpringBeanFactoryTest {

    private static final DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

    @BeforeClass
    public static void init() {
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.setValidating(false);
        reader.setResourceLoader(new PathMatchingResourcePatternResolver());
        reader.loadBeanDefinitions("classpath:spring/beans.xml");
    }

    @Test
    public void testBeanNames() {
        final String[] beanNames = factory.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        final String[] expectedBeans = IntStream.rangeClosed(1, 4).mapToObj(n -> "bean" + n).toArray(String[]::new);
        assertArrayEquals(expectedBeans, beanNames);
    }

    @Test
    public void testSingleton() {
        assertTrue(factory.isSingleton("bean1"));
        assertTrue(factory.isSingleton("bean2"));
    }

    @Test
    public void testMatchBean1() {
        assertTrue(factory.isTypeMatch("bean1", BigInteger.class));
        assertTrue(factory.isTypeMatch("bean1", Number.class));
    }

    @Test
    public void testMatchBean2() {
        assertTrue(factory.isTypeMatch("bean2", Class1.class));
        assertTrue(factory.isTypeMatch("bean2", forClassWithGenerics(Callable.class, Boolean.class)));
        assertTrue(factory.isTypeMatch("bean2", forClassWithGenerics(Callable.class, forType(subtypeOf(Object.class)))));
    }

    @Test
    public void testBean2Type() {
        assertEquals(Class1.class, factory.getType("bean2"));
    }

    @Test
    public void testMatchBean3() {
        assertTrue(factory.isTypeMatch("bean3", String.class));
    }

    @Test
    public void testMatchBean4() {
        assertTrue(factory.isTypeMatch("bean4", Boolean.class));
    }

    @AfterClass
    public static void checkStatics() {
        assertEquals(0, Class1.COUNTER.get());
    }
}
