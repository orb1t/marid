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

import org.marid.misc.Calls;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Dmitry Ovchinnikov
 */
public class URLNoCacheStreamHandler extends URLForwardingStreamHandler {

	public URLNoCacheStreamHandler(URLStreamHandler delegate) {
		super(delegate);
	}

	public URLNoCacheStreamHandler(String protocol) {
		this(delegate(protocol));
	}

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		final URLConnection connection = obtainConnection(u);
		connection.setUseCaches(false);
		return connection;
	}

	@Override
	protected URLConnection openConnection(URL u, Proxy p) throws IOException {
		final URLConnection connection = obtainConnection(u, p);
		connection.setUseCaches(false);
		return connection;
	}

	private static URLStreamHandler delegate(String protocol) {
		return Calls.call(() -> {
			final Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
			method.setAccessible(true);
			return (URLStreamHandler) method.invoke(null, protocol);
		});
	}
}
