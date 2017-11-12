/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.project;

import org.marid.runtime.context.MaridContextListener;

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
  TARGET_CLASSES(TARGET, "classes"),
  CONTEXT_LISTENERS(SERVICES, MaridContextListener.class.getName());

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
