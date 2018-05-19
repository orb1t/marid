/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
package org.marid.applib.spring.init;

import com.google.errorprone.annotations.DoNotCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Initializers {

  @Autowired
  @DoNotCall("Do not call this method directly (it will be called by Spring)")
  default void initInitializers(GenericApplicationContext context) throws ReflectiveOperationException {
    final var methods = Stream.of(getClass().getMethods())
        .filter(m -> m.isAnnotationPresent(Init.class))
        .filter(m -> !Modifier.isStatic(m.getModifiers()))
        .filter(m -> m.canAccess(this))
        .sorted(Comparator.comparingInt(m -> m.getAnnotation(Init.class).value()))
        .toArray(Method[]::new);
    final var beanFactory = context.getDefaultListableBeanFactory();
    for (final var method : methods) {
      final var params = IntStream.range(0, method.getParameterCount())
          .mapToObj(i -> new MethodParameter(method, i))
          .map(mp -> new DependencyDescriptor(mp, true, true))
          .map(dd -> beanFactory.resolveDependency(dd, null))
          .toArray();
      method.invoke(this, params);
    }
  }
}
