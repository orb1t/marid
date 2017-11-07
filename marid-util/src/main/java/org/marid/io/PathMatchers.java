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

package org.marid.io;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface PathMatchers {

	static boolean isClassFile(Path path, BasicFileAttributes a) {
		return path.getFileName() != null && path.getFileName().toString().endsWith(".class");
	}

	static boolean isJarFile(Path path, BasicFileAttributes a) {
		return path.getFileName() != null && path.getFileName().toString().endsWith(".jar");
	}
}
