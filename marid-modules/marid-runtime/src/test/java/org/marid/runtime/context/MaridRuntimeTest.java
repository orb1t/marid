package org.marid.runtime.context;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.beans.BeanFactory;
import org.marid.runtime.beans.BeanInfo;
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
public class MaridRuntimeTest {

    @Test
    public void simple() {
        final BeanInfo[] beans = {
                new BeanInfo(
                        "bean2",
                        new BeanFactory("bean", "bean1"),
                        "getZ",
                        ms(),
                        ms()
                ),
                new BeanInfo(
                        "bean1",
                        new BeanFactory("class", Bean1.class.getName()),
                        "new",
                        ms(m("val", "x", "1"), m("val", "y", "abc"), m("val", "z", "1.23")),
                        ms(m("val", "setA", "true"))
                ),
                new BeanInfo(
                        "bean3",
                        new BeanFactory("bean", "bean1"),
                        "y",
                        ms(),
                        ms()
                ),
                new BeanInfo(
                        "bean4",
                        new BeanFactory("class", Bean1.class.getName()),
                        "list",
                        ms(),
                        ms(m("val", "add", "1"))
                ),
                new BeanInfo(
                        "bean5",
                        new BeanFactory("class", Bean1.class.getName()),
                        "list",
                        ms(),
                        ms(m("val", "add", "1"), m("val", "add", "2"))
                )
        };
        final MaridContext context = new MaridContext(beans);
        try (final MaridRuntime runtime = new MaridRuntime(context, currentThread().getContextClassLoader())) {
            assertEquals(new Bean1(1, "abc", new BigDecimal("1.23")).setA(true), runtime.beans.get("bean1"));
            assertEquals(new BigDecimal("1.23"), runtime.beans.get("bean2"));
            assertEquals("abc", runtime.beans.get("bean3"));
            assertEquals(singletonList(1), runtime.beans.get("bean4"));
            assertEquals(asList(1, 2), runtime.beans.get("bean5"));
        }
    }

    private static BeanMember m(String type, String name, String value) {
        return new BeanMember(type, name, value);
    }

    private static BeanMember[] ms(BeanMember... members) {
        return members;
    }
}
