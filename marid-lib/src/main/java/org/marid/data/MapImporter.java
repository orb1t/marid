/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.data;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import org.marid.util.MutablePropertized;

/**
 * Map importer interface.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface MapImporter extends MutablePropertized {
	/**
	 * Import map from a file.
	 * @param file File that contains a map.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(File file) throws IOException;

	/**
	 * Import map from a path.
	 * @param path Path file object.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(Path path) throws IOException;

	/**
	 * Import map from an url.
	 * @param url URL.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(URL url) throws IOException;

	/**
	 * Import map from an uri.
	 * @param uri URI.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(URI uri) throws IOException;

	/**
	 * Import map from reader.
	 * @param r A reader.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(Reader r) throws IOException;

	/**
	 * Import map from string.
	 * @param s A string containing a map.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(String s) throws IOException;

	/**
	 * Import map from string builder.
	 * @param sb String builder.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(StringBuilder sb) throws IOException;

	/**
	 * Import map from string builder.
	 * @param sb String buffer.
	 * @return Parsed map.
	 * @throws IOException An I/O exception.
	 */
	public Map importMap(StringBuffer sb) throws IOException;
}
