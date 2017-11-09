/*-
 * #%L
 * marid-proto
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

package org.marid.proto.impl.io;

import org.marid.io.IOSupplier;
import org.marid.runtime.annotation.MaridBean;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Dmitry Ovchinnikov
 */
@MaridBean(name = "Standard Socket I/O Supplier")
public class StdProtoSocketIOSupplier implements IOSupplier<StdProtoSocketIO> {

	private InetSocketAddress socketAddress;
	private String host;
	private int port;
	private int connectTimeout;
	private InetAddress address;
	private int soTimeout;
	private Boolean keepAlive;

	@Override
	public StdProtoSocketIO ioGet() throws IOException {
		final Socket socket = new Socket();
		if (soTimeout > 0) {
			socket.setSoTimeout(soTimeout);
		}
		if (keepAlive != null) {
			socket.setKeepAlive(keepAlive);
		}
		if (socketAddress != null) {
			socket.connect(socketAddress, connectTimeout);
		} else if (host != null && port > 0) {
			socket.connect(new InetSocketAddress(host, port));
		} else if (address != null && port > 0) {
			socket.connect(new InetSocketAddress(address, port));
		} else {
			throw new ConnectException("Invalid socket configuration");
		}
		return new StdProtoSocketIO(socket);
	}

	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public Boolean getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
}
