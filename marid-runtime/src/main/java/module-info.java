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

module marid.runtime {

  requires java.annotation;
  requires transitive marid.util;
  requires transitive marid.types;

  exports org.marid.cellar;
  exports org.marid.cellar.common;
  exports org.marid.cellar.runtime;
  exports org.marid.expression.generic;
  exports org.marid.expression.runtime;
  exports org.marid.expression.xml;
  exports org.marid.runtime;
  exports org.marid.runtime.exception;
  exports org.marid.runtime.common;
  exports org.marid.runtime.context;
}