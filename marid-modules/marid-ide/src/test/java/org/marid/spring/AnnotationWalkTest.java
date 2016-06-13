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

package org.marid.spring;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class AnnotationWalkTest {

    @Test
    public void test() {
        final Map<String, Object> expected = ImmutableMap.of("a", 1, "b", 2);
        final Map<String, Object> actual = new TreeMap<>();
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(C.class, W.class)) {
            AnnotatedBean.walk(context, A.class, bean -> actual.put(bean.annotation.x(), bean.annotation.y()));
        }
        assertEquals(expected, actual);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface A {

        String x();

        int y();
    }

    @Configuration
    static class C {

        @Bean
        @A(x = "a", y = 1)
        public String bean1() {
            return "a";
        }
    }

    @Component
    @A(x = "b", y = 2)
    static class W {

    }
}
