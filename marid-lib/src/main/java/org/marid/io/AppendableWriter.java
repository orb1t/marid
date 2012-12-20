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
import java.nio.CharBuffer;

/**
 * Appendable writer.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class AppendableWriter extends Writer {
	
	private Appendable appendable;

	public AppendableWriter(Appendable a) {
		appendable = a;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		appendable.append(CharBuffer.wrap(cbuf, off, len));
	}

	@Override
	public void write(String str) throws IOException {
		appendable.append(str);
	}

	@Override
	public void write(char[] cbuf) throws IOException {
		appendable.append(CharBuffer.wrap(cbuf));
	}

	@Override
	public void write(int c) throws IOException {
		appendable.append((char)c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		appendable.append(str, off, len);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
		appendable = null;
	}	
}
