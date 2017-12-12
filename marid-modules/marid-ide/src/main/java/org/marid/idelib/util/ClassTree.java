/*-
 * #%L
 * marid-ide
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

package org.marid.idelib.util;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public class ClassTree {

  @Nonnull
  public final String name;

  @Nonnull
  public final Class<?>[] classes;

  @Nonnull
  public final ClassTree[] children;

  private ClassTree(@Nonnull String prefix, @Nonnull Map<String, Set<Class<?>>> classes) {
    this.name = prefix;
    this.classes = classes.getOrDefault(prefix, emptySet()).stream()
        .sorted(Comparator.comparing(Class::getName))
        .toArray(Class<?>[]::new);
    this.children = classes.keySet().stream()
        .filter(p -> !p.equals(name) && p.startsWith(name))
        .map(p -> {
          final int index = p.indexOf('.', name.length() + 1);
          return index < 0 ? p : p.substring(0, index);
        })
        .distinct()
        .map(p -> new ClassTree(p, classes))
        .flatMap(ClassTree::trim)
        .sorted(Comparator.comparing(t -> t.name))
        .toArray(ClassTree[]::new);
  }

  public ClassTree(@Nonnull Collection<Class<?>> classes) {
    this("", groupedClasses(classes));
  }

  @Override
  public String toString() {
    return name;
  }

  public Stream<Class<?>> classStream() {
    return Stream.of(classes);
  }

  public Stream<ClassTree> childStream() {
    return Stream.of(children);
  }

  private Stream<ClassTree> trim() {
    if (classes.length == 0 && children.length == 1 && children[0].classes.length == 0) {
      return Stream.of(children[0].children).flatMap(ClassTree::trim);
    } else {
      return Stream.of(this);
    }
  }

  @Nonnull
  private static Map<String, Set<Class<?>>> groupedClasses(@Nonnull Collection<Class<?>> classes) {
    return classes.stream()
        .filter(c -> c.getPackage() != null)
        .collect(Collectors.groupingBy(c -> c.getPackage().getName(), Collectors.toSet()));
  }
}
