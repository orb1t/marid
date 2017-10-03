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

package org.marid.runtime.context2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.runtime.expression.*;
import org.marid.runtime.model.MaridRuntimeBean;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("normal")
class BeanContextTest {

    private static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();
    private static final Properties PROPERTIES = new Properties();

    @Test
    void testOneBean() {
        final BeanConfiguration configuration = new BeanConfiguration(CLASS_LOADER, PROPERTIES);
        final MaridRuntimeBean root = new MaridRuntimeBean()
                .add("bean1", new FieldGetStaticExpr(new ClassExpr(TimeUnit.class.getName()), "SECONDS"))
                .getParent();
        try (final BeanContext context = new BeanContext(configuration, root)) {
            final Object seconds = context.findBean("bean1");
            assertEquals(TimeUnit.SECONDS, seconds);
        }
    }

    @Test
    void testTwoBeans() {
        final BeanConfiguration configuration = new BeanConfiguration(CLASS_LOADER, PROPERTIES);
        final MaridRuntimeBean root = new MaridRuntimeBean()
                .add("bean1", new MethodCallStaticExpr(new ClassExpr(TimeUnit.class.getName()), "valueOf", new RefExpr("bean2")))
                .getParent()
                .add("bean2", new StringExpr("SECONDS"))
                .getParent();
        try (final BeanContext context = new BeanContext(configuration, root)) {
            final Object seconds = context.findBean("bean1");
            assertEquals(TimeUnit.SECONDS, seconds);
        }
    }

    @Test
    void testCloseNestedContextsOnException() {

    }
}
