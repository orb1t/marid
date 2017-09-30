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

package org.marid.runtime.context2.close;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.runtime.context.MaridDefaultContextListener;
import org.marid.runtime.context2.BeanConfiguration;
import org.marid.runtime.context2.BeanContext;
import org.marid.runtime.exception.MaridBeanInitializationException;
import org.marid.runtime.model.MaridRuntimeBean;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.marid.runtime.expression.ExpressionHelper.$;

@Tag("normal")
class BeanContextCloseTest {

    private static final ConcurrentLinkedQueue<String> QUEUE = new ConcurrentLinkedQueue<>();

    @Test
    void closeables() {
        final BeanConfiguration configuration = new BeanConfiguration(
                Thread.currentThread().getContextClassLoader(),
                new Properties(),
                x -> {},
                new MaridDefaultContextListener()
        );
        final MaridRuntimeBean root = new MaridRuntimeBean()
                .add("bean1", $(C1.class))
                .add("bean11", $(C2.class))
                .getParent()
                .getParent()
                .add("bean2", $(C2.class))
                .add("bean21", $(C3.class))
                .add("bean211", $(C4.class))
                .getParent()
                .getParent()
                .getParent();
        try (final BeanContext context = new BeanContext(configuration, root)) {
            // do nothing
        } catch (Throwable x) {
            assertTrue(x instanceof MaridBeanInitializationException);
        }
        assertArrayEquals(new String[] {"c4", "c3", "c2", "c2", "c1"}, QUEUE.toArray(new String[QUEUE.size()]));
    }

    public static class C1 implements AutoCloseable {

        @Override
        public void close() throws Exception {
            QUEUE.add("c1");
        }
    }

    public static class C2 implements AutoCloseable {

        @Override
        public void close() throws Exception {
            QUEUE.add("c2");
        }
    }

    public static class C3 implements AutoCloseable {

        @Override
        public void close() throws Exception {
            QUEUE.add("c3");
        }
    }

    public static class C4 implements AutoCloseable {

        @PostConstruct
        public void init() {
            throw new IllegalStateException();
        }

        @Override
        public void close() throws Exception {
            QUEUE.add("c4");
        }
    }
}
