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
