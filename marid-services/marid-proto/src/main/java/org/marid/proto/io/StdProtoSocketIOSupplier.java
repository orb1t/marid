package org.marid.proto.io;

import org.marid.io.IOSupplier;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Dmitry Ovchinnikov
 */
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
