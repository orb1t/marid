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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.runtime.exception.MaridBeanInitializationException;
import org.marid.expression.runtime.CallExpr;
import org.marid.expression.runtime.ClassExpr;
import org.marid.beans.RuntimeBean;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("normal")
class BeanContextCloseTest {

  private static final ConcurrentLinkedQueue<String> QUEUE = new ConcurrentLinkedQueue<>();

  @Test
  void closeables() {
    final BeanConfiguration configuration = new BeanConfiguration(
        Thread.currentThread().getContextClassLoader(),
        new Properties(),
        new MaridDefaultContextListener()
    );
    final RuntimeBean root = new RuntimeBean()
        .add("bean1", new CallExpr(new ClassExpr(C1.class.getName()), "new"), b -> {
          b.add("bean11", new CallExpr(new ClassExpr(C2.class.getName()), "new"));
        })
        .add("bean2", new CallExpr(new ClassExpr(C2.class.getName()), "new"), b -> {
          b.add("bean21", new CallExpr(new ClassExpr(C3.class.getName()), "new"), bb -> {
            bb.add("bean211", new CallExpr(new ClassExpr(C4.class.getName()), "new"));
          });
        });
    try (final BeanContext context = new BeanContext(configuration, root)) {
      throw new AssertionError("Unreachable");
    } catch (Throwable x) {
      assertTrue(x instanceof MaridBeanInitializationException);
    }
    assertArrayEquals(new String[]{"c4", "c3", "c2", "c2", "c1"}, QUEUE.toArray(new String[QUEUE.size()]));
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
