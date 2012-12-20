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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.marid.util.TreeMapMutablePropertized;

/**
 * Abstract map importer.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class AbstractMapImporter extends
		TreeMapMutablePropertized implements MapImporter {

	@Override
	public Map importMap(File file) throws IOException {
		Charset c = StandardCharsets.UTF_8;
		try (BufferedReader r = Files.newBufferedReader(file.toPath(), c)) {
			return importMap(r);
		}
	}

	@Override
	public Map importMap(Path path) throws IOException {
		Charset c = StandardCharsets.UTF_8;
		try (BufferedReader r = Files.newBufferedReader(path, c)) {
			return importMap(r);
		}
	}

	@Override
	public Map importMap(String s) throws IOException {
		try (StringReader r = new StringReader(s)) {
			return importMap(r);
		}
	}

	@Override
	public Map importMap(StringBuffer sb) throws IOException {
		return importMap(sb.toString()); // TODO: need for better implementation
	}

	@Override
	public Map importMap(StringBuilder sb) throws IOException {
		return importMap(sb.toString()); // TODO: need for better implementation
	}

	@Override
	public Map importMap(URI uri) throws IOException {
		return importMap(uri.toURL());
	}

	@Override
	public Map importMap(URL url) throws IOException {
		Charset c = StandardCharsets.UTF_8;
		try (InputStreamReader r = new InputStreamReader(url.openStream(), c)) {
			return importMap(r);
		}
	}
}
