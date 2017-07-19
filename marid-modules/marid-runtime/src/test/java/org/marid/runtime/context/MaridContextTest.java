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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMember;
import org.marid.test.NormalTests;

import java.math.BigDecimal;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class MaridContextTest {

    @Test
    public void simple() throws Exception {
        final Bean[] beans = {
                new Bean(
                        "bean2",
                        "@bean1",
                        Bean.factory(Bean1.class.getMethod("getZ")),
                        ms(),
                        ms()
                ),
                new Bean(
                        "bean1",
                        Bean1.class.getName(),
                        Bean.factory(Bean1.class.getConstructor(int.class, String.class, BigDecimal.class)),
                        ms(m("int", "x", "1"), m("String", "y", "abc"), m("BigDecimal", "z", "1.23")),
                        ms(m("boolean", "setA", "true"))
                ),
                new Bean(
                        "bean3",
                        "@bean1",
                        Bean.factory(Bean1.class.getField("y")),
                        ms(),
                        ms()
                ),
                new Bean(
                        "bean4",
                        Bean1.class.getName(),
                        Bean.factory(Bean1.class.getMethod("list")),
                        ms(),
                        ms(m("int", "add", "1"))
                ),
                new Bean(
                        "bean5",
                        Bean1.class.getName(),
                        Bean.factory(Bean1.class.getMethod("list")),
                        ms(),
                        ms(m("int", "add", "1"), m("int", "add", "2"))
                ),
                new Bean(
                        "bean6",
                        String.class.getName(),
                        Bean.factory(String.class.getMethod("valueOf", Object.class)),
                        ms(m("js", "arg0", "'a' + 1")),
                        ms()
                )
        };
        final MaridConfiguration context = new MaridConfiguration(beans);
        try (final MaridContext runtime = new MaridContext(context, currentThread().getContextClassLoader())) {
            assertEquals(new Bean1(1, "abc", new BigDecimal("1.23")).setA(true), runtime.beans.get("bean1"));
            assertEquals(new BigDecimal("1.23"), runtime.beans.get("bean2"));
            assertEquals("abc", runtime.beans.get("bean3"));
            assertEquals(singletonList(1), runtime.beans.get("bean4"));
            assertEquals(asList(1, 2), runtime.beans.get("bean5"));
            assertEquals("a1", runtime.beans.get("bean6"));
            assertEquals(10, ((Bean1) runtime.beans.get("bean1")).q);
        }
    }

    private static BeanMember m(String type, String name, String value) {
        return new BeanMember(type, name, value);
    }

    private static BeanMember[] ms(BeanMember... members) {
        return members;
    }
}