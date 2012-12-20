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
package org.marid.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Metered writer.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MeteredWriter extends Writer implements MeteredWrite {
	
	private long count;
	private Writer writer;
	
	/**
	 * Constructs the metered writer.
	 * @param w Wrapped writer.
	 */
	public MeteredWriter(Writer w) {
		writer = w;
	}

	@Override
	public void write(String str) throws IOException {
		writer.write(str);
		count += str.length();
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		writer.write(cbuf);
		count += cbuf.length;
	}

	@Override
	public void write(int c) throws IOException {
		writer.write(c);
		count++;
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		writer.write(str, off, len);
		count += len;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		writer.write(cbuf, off, len);
		count += len;
	}

	@Override
	public void close() throws IOException {
		try {
			writer.close();
		} finally {
			writer = null;
		}
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public long getWrittenCount() {
		return count;
	}
}
