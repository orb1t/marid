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

package org.marid.ide.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class FileFilters {

  @Bean({"text/x-java"})
  @Qualifier("java")
  public PathMatcher javaPathMatcher() {
    return p -> FileSystems.getDefault().getPathMatcher("glob:*.java").matches(p.getFileName());
  }

  @Bean({"application/java-archive"})
  @Qualifier("jar")
  public PathMatcher jarPathMatcher() {
    return p -> FileSystems.getDefault().getPathMatcher("glob:*.jar").matches(p.getFileName());
  }

  @Bean({"text/x-java-properties"})
  @Qualifier("properties")
  public PathMatcher propertiesPathMatcher() {
    return p -> FileSystems.getDefault().getPathMatcher("glob:*.properties").matches(p.getFileName());
  }

  @Bean({"text/plain"})
  @Qualifier("lst")
  public PathMatcher confListPathMatcher() {
    return p -> FileSystems.getDefault().getPathMatcher("glob:*.lst").matches(p.getFileName());
  }

  @Bean
  @Qualifier("text-files")
  public PathMatcher textFilePathMatcher(Map<String, PathMatcher> pathMatcherMap) {
    return pathMatcherMap.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith("text/"))
        .map(Entry::getValue)
        .reduce((p1, p2) -> p -> p1.matches(p) || p2.matches(p))
        .orElseThrow(() -> new IllegalStateException("No text type filters"));
  }
}
