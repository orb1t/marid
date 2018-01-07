/*-
 * #%L
 * marid-util
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

module marid.util {

  requires transitive java.logging;
  requires transitive java.xml;
  requires static transitive marid.intellij.annotations;

  requires static java.desktop;
  requires static javafx.base;
  requires static javafx.graphics;
  requires static javafx.swing;

  exports org.marid.cache;
  exports org.marid.collections;
  exports org.marid.concurrent;
  exports org.marid.function;
  exports org.marid.image;
  exports org.marid.io;
  exports org.marid.l10n;
  exports org.marid.logging;
  exports org.marid.misc;
  exports org.marid.xml;
}
