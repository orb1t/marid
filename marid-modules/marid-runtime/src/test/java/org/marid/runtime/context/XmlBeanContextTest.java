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

package org.marid.runtime.context;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.marid.beans.RuntimeBean;
import org.marid.io.Xmls;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlBeanContextTest {

    private static Properties properties;
    private static ClassLoader classLoader;
    private static BeanConfiguration beanConfiguration;
    private static BeanContext context;

    @BeforeAll
    static void init() throws IOException {
        properties = PropertiesLoaderUtils.loadAllProperties("classpath:beans.properties");
        classLoader = Thread.currentThread().getContextClassLoader();
        beanConfiguration = new BeanConfiguration(classLoader, properties);

        try (final Reader reader = new InputStreamReader(classLoader.getResourceAsStream("beans1.xml"), UTF_8)) {
            context = new BeanContext(beanConfiguration, Xmls.read(reader, e -> new RuntimeBean(null, e)));
        }
    }

    @AfterAll
    static void destroy() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void testB1() {
        final BeanContext b = context.children()
                .filter(c -> c.getBean().getName().equals("b1"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("b1"));
        assertEquals("str", b.getInstance());
        assertEquals(String.class, b.getType());
    }

    @Test
    void testB2() {
        final BeanContext b = context.children()
                .filter(c -> c.getBean().getName().equals("b2"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("b2"));
        assertEquals(BigInteger.valueOf(1L), b.getInstance());
        assertEquals(BigInteger.class, b.getType());
    }

    @Test
    void testB3() {
        final BeanContext b = context.children()
                .filter(c -> c.getBean().getName().equals("b3"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("b3"));
        assertEquals(singletonList(BigInteger.valueOf(1L)), b.getInstance());
        assertEquals(ArrayList.class, b.getType());
    }
}
