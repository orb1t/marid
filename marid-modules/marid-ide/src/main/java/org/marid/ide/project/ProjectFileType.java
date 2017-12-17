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

package org.marid.ide.project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public enum ProjectFileType {

  POM("pom.xml"),
  SRC("src"),
  TARGET("target"),
  SRC_MAIN(SRC, "main"),
  SRC_TEST(SRC, "test"),
  SRC_MAIN_JAVA(SRC_MAIN, "java"),
  SRC_MAIN_RESOURCES(SRC_MAIN, "resources"),
  SRC_TEST_JAVA(SRC_TEST, "java"),
  SRC_TEST_RESOURCES(SRC_TEST, "resources"),
  META_INF(SRC_MAIN_RESOURCES, "META-INF"),
  META_DIR(META_INF, "marid"),
  SERVICES(META_INF, "services"),
  APPLICATION_PROPERTIES(SRC_MAIN_RESOURCES, "application.properties"),
  BEANS_XML(META_DIR, "beans.xml"),
  TARGET_LIB(TARGET, "lib"),
  TARGET_CLASSES(TARGET, "classes");

  public final Path relative;

  ProjectFileType(String arg, String... parts) {
    this.relative = Paths.get(arg, parts);
  }

  ProjectFileType(ProjectFileType base, String... parts) {
    this.relative = Stream.of(parts).reduce(base.relative, Path::resolve, (p1, p2) -> p2);
  }

  public Path toFull(Path base) {
    return base.resolve(relative);
  }

  public boolean isDirectory() {
    return relative.getFileName().toString().indexOf('.') < 0;
  }
}
